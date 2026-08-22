package com.fincore.transfers.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.accounts.dto.AccountTransferResult;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.beneficiaries.service.BeneficiaryService;
import com.fincore.beneficiaries.dto.BeneficiaryInfo;
import com.fincore.customers.service.CustomerProfileService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.ledger.service.LedgerService;
import com.fincore.shared.exception.ConflictException;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.exception.ResourceNotFoundException;
import com.fincore.transfers.dto.TransferCommand;
import com.fincore.transfers.dto.TransferView;
import com.fincore.transfers.entity.FinancialTransfer;
import com.fincore.transfers.mapper.TransferMapper;
import com.fincore.transfers.repository.FinancialTransferRepository;
import com.fincore.transfers.repository.TransferIdempotencyRepository;
import com.fincore.transfers.repository.TransferIdempotencyRepository.IdempotencyClaim;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferExecutionService {

    private final FinancialTransferRepository transferRepository;
    private final TransferIdempotencyRepository idempotencyRepository;
    private final CustomerProfileService customerService;
    private final BeneficiaryService beneficiaryService;
    private final FinancialAccountService accountService;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final TransferMapper mapper;
    private final Clock clock;
    private final BigDecimal maximumAmount;

    TransferExecutionService(
            FinancialTransferRepository transferRepository,
            TransferIdempotencyRepository idempotencyRepository,
            CustomerProfileService customerService,
            BeneficiaryService beneficiaryService,
            FinancialAccountService accountService,
            LedgerService ledgerService,
            AuditService auditService,
            TransferMapper mapper,
            Clock clock,
            @Value("${fincore.transfers.maximum-amount}") BigDecimal maximumAmount) {
        this.transferRepository = transferRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.customerService = customerService;
        this.beneficiaryService = beneficiaryService;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.mapper = mapper;
        this.clock = clock;
        this.maximumAmount = maximumAmount;
    }

    /**
     * Toda la operación vive en una sola transacción ACID: idempotencia, saldos,
     * transferencia, ledger y auditoría se confirman o se revierten juntos.
     */
    @Transactional
    public TransferView execute(String idempotencyKey, TransferCommand command, AuthenticatedUser user) {
        BigDecimal amount = normalizeAmount(command.amount());
        String description = normalizeDescription(command.description());
        String requestHash = hash(command.sourceAccountId(), command.beneficiaryId(), amount, description);
        Instant now = Instant.now(clock);
        IdempotencyClaim claim = idempotencyRepository.claim(user.id(), idempotencyKey, requestHash, now);

        if (!claim.requestHash().equals(requestHash)) {
            throw new ConflictException("La clave de idempotencia ya fue usada con otra solicitud.");
        }
        if (!claim.newlyCreated()) {
            if (claim.transferId() == null) {
                throw new ConflictException("La solicitud con esta clave todavía está en procesamiento.");
            }
            return toView(requireTransfer(claim.transferId()));
        }

        UUID customerId = customerService.requireActiveByUserId(user.id()).id();
        BeneficiaryInfo beneficiary = beneficiaryService.requireOwnedActive(
                command.beneficiaryId(), customerId);
        AccountTransferResult balances = accountService.transfer(
                customerId,
                command.sourceAccountId(),
                beneficiary.destinationAccountId(),
                amount);

        UUID transferId = UUID.randomUUID();
        // Flush hace visible la fila para la FK que completará la reserva de idempotencia.
        FinancialTransfer transfer = transferRepository.saveAndFlush(new FinancialTransfer(
                transferId,
                generateReference(transferId),
                user.id(),
                balances.sourceAccountId(),
                balances.destinationAccountId(),
                beneficiary.id(),
                balances.currency(),
                amount,
                description,
                now));
        ledgerService.recordTransfer(
                transferId,
                balances.currency(),
                balances.sourceAccountId(),
                balances.destinationAccountId(),
                amount,
                description == null ? "Transferencia interna" : description,
                user.getUsername(),
                now);
        idempotencyRepository.complete(claim.reservationId(), transferId, now);
        auditService.record(
                user.getUsername(),
                "TRANSFER_CONFIRMED",
                AuditOutcome.SUCCESS,
                "TRANSFER",
                transferId.toString(),
                "Referencia: " + generateReference(transferId));
        return mapper.toView(transfer,
                accountService.requireById(balances.sourceAccountId()),
                accountService.requireById(balances.destinationAccountId()));
    }

    private BigDecimal normalizeAmount(BigDecimal rawAmount) {
        try {
            BigDecimal amount = rawAmount.setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() <= 0) {
                throw new OperationNotAllowedException("El monto debe ser mayor que cero.");
            }
            if (amount.compareTo(maximumAmount) > 0) {
                throw new OperationNotAllowedException("El monto supera el máximo permitido.");
            }
            return amount;
        } catch (ArithmeticException exception) {
            throw new OperationNotAllowedException("El monto admite como máximo dos decimales.");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private String hash(UUID source, UUID beneficiary, BigDecimal amount, String description) {
        String canonical = source + "|" + beneficiary + "|" + amount.toPlainString() + "|"
                + (description == null ? "" : description);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("La JVM no ofrece SHA-256.", exception);
        }
    }

    private String generateReference(UUID transferId) {
        return "TRX-" + transferId.toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private FinancialTransfer requireTransfer(UUID transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("La transferencia no existe."));
    }

    private TransferView toView(FinancialTransfer transfer) {
        return mapper.toView(transfer,
                accountService.requireById(transfer.sourceAccountId()),
                accountService.requireById(transfer.destinationAccountId()));
    }
}
