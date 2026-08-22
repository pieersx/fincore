package com.fincore.beneficiaries.dto;

import java.util.UUID;

/** Datos internos mínimos necesarios para validar una transferencia. */
public record BeneficiaryInfo(UUID id, UUID destinationAccountId) {
}
