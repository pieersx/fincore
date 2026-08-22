package com.fincore.beneficiaries.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.accounts.dto.AccountInfo;
import com.fincore.accounts.entity.AccountKind;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.beneficiaries.dto.BeneficiaryInfo;
import com.fincore.beneficiaries.dto.BeneficiaryView;
import com.fincore.beneficiaries.entity.Beneficiary;
import com.fincore.beneficiaries.mapper.BeneficiaryMapper;
import com.fincore.beneficiaries.repository.BeneficiaryRepository;
import com.fincore.customers.service.CustomerProfileService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.shared.exception.ConflictException;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final CustomerProfileService customerService;
    private final FinancialAccountService accountService;
    private final AuditService auditService;
    private final BeneficiaryMapper mapper;
    private final Clock clock;

    BeneficiaryService(
            BeneficiaryRepository repository,
            CustomerProfileService customerService,
            FinancialAccountService accountService,
            AuditService auditService,
            BeneficiaryMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.customerService = customerService;
        this.accountService = accountService;
        this.auditService = auditService;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryView> findOwn(AuthenticatedUser user) {
        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        return repository.findByOwnerCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(customerId)
                .stream()
                .map(beneficiary -> mapper.toView(
                        beneficiary,
                        accountService.requireById(beneficiary.destinationAccountId())))
                .toList();
    }

    @Transactional
    public BeneficiaryView create(String accountNumber, String alias, AuthenticatedUser user) {
        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        AccountInfo destination = accountService.requireByNumber(accountNumber);
        if (destination.kind() != AccountKind.CUSTOMER || destination.status() != AccountStatus.ACTIVE) {
            throw new OperationNotAllowedException("La cuenta destino no está habilitada para recibir transferencias.");
        }
        if (customerId.equals(destination.customerId())) {
            throw new OperationNotAllowedException("No se puede registrar una cuenta propia como beneficiario.");
        }
        if (repository.existsByOwnerCustomerIdAndDestinationAccountIdAndDeletedAtIsNull(
                customerId, destination.id())) {
            throw new ConflictException("La cuenta ya está registrada como beneficiario.");
        }

        Beneficiary beneficiary = repository.save(new Beneficiary(
                UUID.randomUUID(),
                customerId,
                destination.id(),
                alias.trim(),
                Instant.now(clock)));
        auditService.record(
                user.getUsername(),
                "BENEFICIARY_CREATED",
                AuditOutcome.SUCCESS,
                "BENEFICIARY",
                beneficiary.id().toString(),
                "Cuenta destino: " + destination.accountNumber());
        return mapper.toView(beneficiary, destination);
    }

    @Transactional
    public void delete(UUID beneficiaryId, AuthenticatedUser user) {
        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        Beneficiary beneficiary = requireEntity(beneficiaryId, customerId);
        beneficiary.delete(Instant.now(clock));
        auditService.record(
                user.getUsername(),
                "BENEFICIARY_DELETED",
                AuditOutcome.SUCCESS,
                "BENEFICIARY",
                beneficiaryId.toString(),
                "Borrado lógico");
    }

    @Transactional(readOnly = true)
    public BeneficiaryInfo requireOwnedActive(UUID beneficiaryId, UUID ownerCustomerId) {
        return mapper.toInfo(requireEntity(beneficiaryId, ownerCustomerId));
    }

    private Beneficiary requireEntity(UUID beneficiaryId, UUID customerId) {
        return repository.findByIdAndOwnerCustomerIdAndDeletedAtIsNull(beneficiaryId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("El beneficiario no existe para este cliente."));
    }
}
