package com.fincore.accounts.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fincore.accounts.dto.AccountInfo;
import com.fincore.accounts.entity.AccountKind;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.accounts.dto.AccountTransferResult;
import com.fincore.accounts.dto.AccountView;
import com.fincore.accounts.dto.ReconciliationItem;
import com.fincore.accounts.dto.ReconciliationView;
import com.fincore.accounts.entity.FinancialAccount;
import com.fincore.accounts.mapper.AccountMapper;
import com.fincore.accounts.repository.FinancialAccountRepository;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.customers.service.CustomerProfileService;
import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.ledger.dto.LedgerMovementView;
import com.fincore.ledger.service.LedgerService;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.model.Currency;
import com.fincore.shared.exception.ResourceNotFoundException;
import com.fincore.shared.dto.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialAccountService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final FinancialAccountRepository repository;
    private final CustomerProfileService customerService;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final AccountMapper mapper;
    private final Clock clock;

    FinancialAccountService(
            FinancialAccountRepository repository,
            CustomerProfileService customerService,
            LedgerService ledgerService,
            AuditService auditService,
            AccountMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.customerService = customerService;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** Cada cliente nuevo recibe una cuenta PEN y una USD, ambas con saldo cero. */
    @Transactional
    public List<AccountView> provisionDefaultAccounts(UUID customerId) {
        if (!repository.findByCustomerIdOrderByCurrency(customerId).isEmpty()) {
            throw new OperationNotAllowedException("El cliente ya tiene cuentas provisionadas.");
        }
        Instant now = Instant.now(clock);
        return List.of(Currency.PEN, Currency.USD).stream()
                .map(currency -> mapper.toView(repository.save(new FinancialAccount(
                        UUID.randomUUID(),
                        customerId,
                        generateAccountNumber(currency),
                        AccountKind.CUSTOMER,
                        currency,
                        ZERO,
                        now))))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountView> findOwnAccounts(AuthenticatedUser user) {
        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        return repository.findByCustomerIdOrderByCurrency(customerId).stream()
                .map(mapper::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountView findOwnAccount(UUID accountId, AuthenticatedUser user) {
        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        FinancialAccount account = requireEntity(accountId);
        requireOwnership(account, customerId);
        return mapper.toView(account);
    }

    @Transactional(readOnly = true)
    public PageResponse<LedgerMovementView> findOwnMovements(
            UUID accountId,
            AuthenticatedUser user,
            int page,
            int size) {
        findOwnAccount(accountId, user);
        return ledgerService.findMovements(accountId, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountView> findAll(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(repository.findAll(request), mapper::toView);
    }

    @Transactional(readOnly = true)
    public AccountView findById(UUID accountId) {
        return mapper.toView(requireEntity(accountId));
    }

    @Transactional
    public AccountView changeStatus(
            UUID accountId,
            AccountStatus newStatus,
            AuthenticatedUser administrator) {
        FinancialAccount account = requireEntity(accountId);
        if (account.kind() == AccountKind.SYSTEM) {
            throw new OperationNotAllowedException("Las cuentas internas no se suspenden desde la API.");
        }
        account.changeStatus(newStatus, Instant.now(clock));
        auditService.record(
                administrator.getUsername(),
                "ACCOUNT_STATUS_CHANGED",
                AuditOutcome.SUCCESS,
                "ACCOUNT",
                accountId.toString(),
                "Nuevo estado: " + newStatus);
        return mapper.toView(account);
    }

    @Transactional(readOnly = true)
    public AccountInfo requireById(UUID accountId) {
        return mapper.toInfo(requireEntity(accountId));
    }

    @Transactional(readOnly = true)
    public AccountInfo requireByNumber(String accountNumber) {
        return repository.findByAccountNumberIgnoreCase(accountNumber.trim())
                .map(mapper::toInfo)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta destino no existe."));
    }

    /**
     * Bloquea ambas cuentas en orden estable para reducir deadlocks y actualiza los saldos
     * dentro de la misma transacción que guarda la transferencia y el ledger.
     */
    @Transactional
    public AccountTransferResult transfer(
            UUID ownerCustomerId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new OperationNotAllowedException("La cuenta origen y destino deben ser diferentes.");
        }
        UUID firstId = sourceAccountId.compareTo(destinationAccountId) < 0
                ? sourceAccountId : destinationAccountId;
        UUID secondId = firstId.equals(sourceAccountId) ? destinationAccountId : sourceAccountId;

        FinancialAccount first = requireLocked(firstId);
        FinancialAccount second = requireLocked(secondId);
        FinancialAccount source = first.id().equals(sourceAccountId) ? first : second;
        FinancialAccount destination = first.id().equals(destinationAccountId) ? first : second;

        requireOwnership(source, ownerCustomerId);
        if (destination.kind() != AccountKind.CUSTOMER) {
            throw new OperationNotAllowedException("La cuenta destino no pertenece a un cliente.");
        }
        if (source.currency() != destination.currency()) {
            throw new OperationNotAllowedException("Las transferencias deben usar cuentas de la misma moneda.");
        }

        Instant now = Instant.now(clock);
        source.debit(amount, now);
        destination.credit(amount, now);
        return new AccountTransferResult(
                source.id(),
                source.accountNumber(),
                source.balance(),
                destination.id(),
                destination.accountNumber(),
                destination.balance(),
                source.currency());
    }

    @Transactional(readOnly = true)
    public ReconciliationView reconcile() {
        Map<UUID, BigDecimal> ledgerBalances = ledgerService.reconstructBalances();
        List<ReconciliationItem> items = repository.findAll().stream()
                .map(account -> {
                    BigDecimal ledgerBalance = ledgerBalances.getOrDefault(account.id(), ZERO);
                    BigDecimal difference = account.balance().subtract(ledgerBalance);
                    return new ReconciliationItem(
                            account.id(),
                            account.accountNumber(),
                            account.kind(),
                            account.currency(),
                            account.balance(),
                            ledgerBalance,
                            difference,
                            difference.compareTo(ZERO) == 0);
                })
                .toList();
        long mismatches = items.stream().filter(item -> !item.balanced()).count();
        return new ReconciliationView(Instant.now(clock), mismatches == 0, mismatches, items);
    }

    private FinancialAccount requireEntity(UUID accountId) {
        return repository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe."));
    }

    private FinancialAccount requireLocked(UUID accountId) {
        return repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe."));
    }

    private void requireOwnership(FinancialAccount account, UUID customerId) {
        if (account.kind() != AccountKind.CUSTOMER || !customerId.equals(account.customerId())) {
            throw new ResourceNotFoundException("La cuenta no pertenece al cliente autenticado.");
        }
    }

    private String generateAccountNumber(Currency currency) {
        for (int attempt = 0; attempt < 5; attempt++) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 13).toUpperCase();
            String candidate = "FC" + currency.name() + suffix;
            if (!repository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No se pudo generar un número de cuenta único.");
    }
}
