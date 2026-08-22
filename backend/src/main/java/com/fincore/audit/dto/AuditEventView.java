package com.fincore.audit.dto;

import java.time.Instant;
import java.util.UUID;

import com.fincore.audit.entity.AuditOutcome;

/** Representación de solo lectura que puede exponerse a analistas y administradores. */
public record AuditEventView(
        UUID id,
        String actorUsername,
        String action,
        AuditOutcome outcome,
        String resourceType,
        String resourceId,
        String correlationId,
        String detail,
        Instant occurredAt) {
}
