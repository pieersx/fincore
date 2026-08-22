package com.fincore.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.ledger.entity.LedgerEntryType;
import com.fincore.ledger.entity.LedgerReferenceType;
import com.fincore.shared.model.Currency;

/** Una línea del ledger presentada como movimiento de una cuenta. */
public record LedgerMovementView(
        UUID entryId,
        UUID journalId,
        LedgerReferenceType referenceType,
        UUID referenceId,
        LedgerEntryType type,
        BigDecimal amount,
        Currency currency,
        String description,
        Instant occurredAt) {

    /** Los créditos suman al saldo de cliente y los débitos restan. */
    public BigDecimal signedAmount() {
        return type == LedgerEntryType.CREDIT ? amount : amount.negate();
    }
}
