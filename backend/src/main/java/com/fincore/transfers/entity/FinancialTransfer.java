package com.fincore.transfers.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.transfers.entity.TransferStatus;
import com.fincore.shared.model.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "financial_transfer")
public class FinancialTransfer {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(name = "beneficiary_id", nullable = false)
    private UUID beneficiaryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(length = 140)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected FinancialTransfer() {
    }

    public FinancialTransfer(
            UUID id,
            String reference,
            UUID createdByUserId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            UUID beneficiaryId,
            Currency currency,
            BigDecimal amount,
            String description,
            Instant occurredAt) {
        this.id = id;
        this.reference = reference;
        this.createdByUserId = createdByUserId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.beneficiaryId = beneficiaryId;
        this.currency = currency;
        this.amount = amount;
        this.status = TransferStatus.CONFIRMED;
        this.description = description;
        this.createdAt = occurredAt;
        this.completedAt = occurredAt;
    }

    public UUID id() {
        return id;
    }

    public UUID createdByUserId() {
        return createdByUserId;
    }

    public UUID sourceAccountId() {
        return sourceAccountId;
    }

    public UUID destinationAccountId() {
        return destinationAccountId;
    }

    public String reference() {
        return reference;
    }

    public UUID beneficiaryId() {
        return beneficiaryId;
    }

    public Currency currency() {
        return currency;
    }

    public BigDecimal amount() {
        return amount;
    }

    public TransferStatus status() {
        return status;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant completedAt() {
        return completedAt;
    }
}
