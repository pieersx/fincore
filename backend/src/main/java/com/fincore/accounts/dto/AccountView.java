package com.fincore.accounts.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.accounts.entity.AccountKind;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.shared.model.Currency;

/** Representación segura de una cuenta; no expone la entidad persistente. */
public record AccountView(
        UUID id,
        UUID customerId,
        String accountNumber,
        AccountKind kind,
        Currency currency,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt) {
}
