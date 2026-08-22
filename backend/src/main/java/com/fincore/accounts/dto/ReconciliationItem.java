package com.fincore.accounts.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fincore.accounts.entity.AccountKind;
import com.fincore.shared.model.Currency;

/** Comparación entre el saldo operativo y el saldo reconstruido desde el ledger. */
public record ReconciliationItem(
        UUID accountId,
        String accountNumber,
        AccountKind kind,
        Currency currency,
        BigDecimal storedBalance,
        BigDecimal ledgerBalance,
        BigDecimal difference,
        boolean balanced) {
}
