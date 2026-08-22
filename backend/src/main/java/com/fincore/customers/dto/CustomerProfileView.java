package com.fincore.customers.dto;

import java.time.Instant;
import java.util.UUID;

import com.fincore.customers.entity.CustomerStatus;

/** Contrato público del perfil sin asociaciones JPA ni datos financieros. */
public record CustomerProfileView(
        UUID id,
        UUID userId,
        String displayName,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
