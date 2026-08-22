package com.fincore.transfers.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo HTTP validado para solicitar una transferencia. */
public record CreateTransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID beneficiaryId,
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,
        @Size(max = 140) String description) {
}
