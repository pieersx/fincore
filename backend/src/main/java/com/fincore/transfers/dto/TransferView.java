package com.fincore.transfers.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.shared.model.Currency;
import com.fincore.transfers.entity.TransferStatus;

public record TransferView(
        UUID id,
        String reference,
        UUID createdByUserId,
        UUID sourceAccountId,
        String sourceAccountNumber,
        UUID destinationAccountId,
        String destinationAccountNumber,
        UUID beneficiaryId,
        Currency currency,
        BigDecimal amount,
        TransferStatus status,
        String description,
        Instant createdAt,
        Instant completedAt) {
}
