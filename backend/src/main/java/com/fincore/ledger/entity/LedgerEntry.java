package com.fincore.ledger.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.ledger.entity.LedgerEntryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_id", nullable = false)
    private LedgerJournal journal;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(
            UUID id,
            LedgerJournal journal,
            UUID accountId,
            LedgerEntryType entryType,
            BigDecimal amount,
            Instant createdAt) {
        this.id = id;
        this.journal = journal;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
