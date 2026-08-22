package com.fincore.beneficiaries.dto;

import java.time.Instant;
import java.util.UUID;

import com.fincore.shared.model.Currency;

public record BeneficiaryView(
        UUID id,
        UUID destinationAccountId,
        String destinationAccountNumber,
        Currency currency,
        String alias,
        Instant createdAt) {
}
