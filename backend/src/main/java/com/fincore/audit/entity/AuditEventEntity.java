package com.fincore.audit.entity;

import java.time.Instant;
import java.util.UUID;

import com.fincore.audit.entity.AuditOutcome;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

    @Id
    private UUID id;

    @Column(name = "actor_username", length = 50)
    private String actorUsername;

    @Column(nullable = false, length = 60)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditOutcome outcome;

    @Column(name = "resource_type", length = 60)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(length = 500)
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEventEntity() {
    }

    public AuditEventEntity(
            UUID id,
            String actorUsername,
            String action,
            AuditOutcome outcome,
            String resourceType,
            String resourceId,
            String correlationId,
            String detail,
            Instant occurredAt) {
        this.id = id;
        this.actorUsername = actorUsername;
        this.action = action;
        this.outcome = outcome;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.correlationId = correlationId;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public UUID id() {
        return id;
    }

    public String actorUsername() {
        return actorUsername;
    }

    public String action() {
        return action;
    }

    public AuditOutcome outcome() {
        return outcome;
    }

    public String resourceType() {
        return resourceType;
    }

    public String resourceId() {
        return resourceId;
    }

    public String correlationId() {
        return correlationId;
    }

    public String detail() {
        return detail;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
