package com.fincore.transfers.service;

import java.util.UUID;

import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.shared.exception.ResourceNotFoundException;
import com.fincore.shared.dto.PageResponse;
import com.fincore.transfers.dto.TransferCommand;
import com.fincore.transfers.dto.TransferView;
import com.fincore.transfers.entity.FinancialTransfer;
import com.fincore.transfers.mapper.TransferMapper;
import com.fincore.transfers.repository.FinancialTransferRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final TransferExecutionService executionService;
    private final FinancialTransferRepository repository;
    private final FinancialAccountService accountService;
    private final AuditService auditService;
    private final TransferMapper mapper;

    TransferService(
            TransferExecutionService executionService,
            FinancialTransferRepository repository,
            FinancialAccountService accountService,
            AuditService auditService,
            TransferMapper mapper) {
        this.executionService = executionService;
        this.repository = repository;
        this.accountService = accountService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    /** Registra aparte los rechazos porque la transacción financiera ya fue revertida. */
    public TransferView create(String idempotencyKey, TransferCommand command, AuthenticatedUser user) {
        try {
            return executionService.execute(idempotencyKey, command, user);
        } catch (RuntimeException exception) {
            auditService.record(
                    user.getUsername(),
                    "TRANSFER_REJECTED",
                    AuditOutcome.FAILURE,
                    "TRANSFER_REQUEST",
                    idempotencyKey,
                    exception.getClass().getSimpleName() + ": " + exception.getMessage());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferView> findOwn(AuthenticatedUser user, int page, int size) {
        return PageResponse.from(
                repository.findByCreatedByUserId(
                        user.id(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                this::toView);
    }

    @Transactional(readOnly = true)
    public TransferView findOwnById(UUID transferId, AuthenticatedUser user) {
        return repository.findByIdAndCreatedByUserId(transferId, user.id())
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("La transferencia no existe para este cliente."));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferView> findAll(int page, int size) {
        return PageResponse.from(
                repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())),
                this::toView);
    }

    @Transactional(readOnly = true)
    public TransferView findById(UUID transferId) {
        return repository.findById(transferId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("La transferencia no existe."));
    }

    private TransferView toView(FinancialTransfer transfer) {
        return mapper.toView(transfer,
                accountService.requireById(transfer.sourceAccountId()),
                accountService.requireById(transfer.destinationAccountId()));
    }
}
