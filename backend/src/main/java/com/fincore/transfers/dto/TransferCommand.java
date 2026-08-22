package com.fincore.transfers.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Comando interno ya validado por Bean Validation en el borde HTTP. */
public record TransferCommand(
        UUID sourceAccountId,
        UUID beneficiaryId,
        BigDecimal amount,
        String description) {
}
