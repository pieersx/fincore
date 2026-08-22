package com.fincore.accounts.dto;

import java.util.UUID;

import com.fincore.accounts.entity.AccountKind;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.shared.model.Currency;

/** Datos mínimos compartidos con beneficiarios y transferencias. */
public record AccountInfo(
        UUID id,
        UUID customerId,
        String accountNumber,
        AccountKind kind,
        Currency currency,
        AccountStatus status) {
}
