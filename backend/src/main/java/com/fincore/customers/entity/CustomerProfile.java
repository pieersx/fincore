package com.fincore.customers.entity;

import java.time.Instant;
import java.util.UUID;

import com.fincore.customers.entity.CustomerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "customer")
public class CustomerProfile {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected CustomerProfile() {
    }

    public CustomerProfile(UUID id, UUID userId, String displayName, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.status = CustomerStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String displayName() {
        return displayName;
    }

    public CustomerStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    /** Suspender no elimina el perfil ni rompe la trazabilidad histórica. */
    public void changeStatus(CustomerStatus newStatus, Instant changedAt) {
        status = newStatus;
        updatedAt = changedAt;
    }

}
