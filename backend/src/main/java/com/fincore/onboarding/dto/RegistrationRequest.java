package com.fincore.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos públicos requeridos para registrar un cliente. */
public record RegistrationRequest(
        @NotBlank
        @Pattern(
                regexp = "[a-zA-Z0-9._-]{4,50}",
                message = "debe tener entre 4 y 50 caracteres alfanuméricos o . _ -")
        String username,
        @NotBlank
        @Size(min = 12, max = 72, message = "debe tener entre 12 y 72 caracteres")
        String password,
        @NotBlank
        @Size(min = 2, max = 120, message = "debe tener entre 2 y 120 caracteres")
        @Pattern(
                regexp = "\\s*\\S.*\\S\\s*",
                message = "debe contener al menos 2 caracteres visibles")
        String displayName) {
}
