package com.fincore.ledger.entity;

import java.time.Instant;
import java.util.UUID;

import com.fincore.ledger.entity.LedgerReferenceType;
import com.fincore.shared.model.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_journal")
public class LedgerJournal {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 30)
    private LedgerReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, length = 140)
    private String description;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LedgerJournal() {
    }

    public LedgerJournal(
            UUID id,
            LedgerReferenceType referenceType,
            UUID referenceId,
            Currency currency,
            String description,
            String createdBy,
            Instant occurredAt) {
        this.id = id;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.currency = currency;
        this.description = description;
        this.createdBy = createdBy;
        this.occurredAt = occurredAt;
    }

    public UUID id() {
        return id;
    }
}
