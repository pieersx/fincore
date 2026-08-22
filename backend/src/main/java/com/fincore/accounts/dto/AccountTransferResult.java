package com.fincore.accounts.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fincore.shared.model.Currency;

/** Instantánea resultante del cambio de saldos realizada dentro de la transacción. */
public record AccountTransferResult(
        UUID sourceAccountId,
        String sourceAccountNumber,
        BigDecimal sourceBalance,
        UUID destinationAccountId,
        String destinationAccountNumber,
        BigDecimal destinationBalance,
        Currency currency) {
}
