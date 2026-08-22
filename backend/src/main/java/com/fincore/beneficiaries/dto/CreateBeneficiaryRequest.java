package com.fincore.beneficiaries.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos validados para registrar una cuenta beneficiaria. */
public record CreateBeneficiaryRequest(
        @NotBlank
        @Pattern(regexp = "FC(PEN|USD)[A-Z0-9]{10,15}", message = "no tiene un formato FinCore válido")
        String destinationAccountNumber,
        @NotBlank @Size(min = 2, max = 80) String alias) {
}
