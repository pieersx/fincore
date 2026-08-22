package com.fincore.beneficiaries.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "beneficiary")
public class Beneficiary {

    @Id
    private UUID id;

    @Column(name = "owner_customer_id", nullable = false)
    private UUID ownerCustomerId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(nullable = false, length = 80)
    private String alias;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    protected Beneficiary() {
    }

    public Beneficiary(
            UUID id,
            UUID ownerCustomerId,
            UUID destinationAccountId,
            String alias,
            Instant createdAt) {
        this.id = id;
        this.ownerCustomerId = ownerCustomerId;
        this.destinationAccountId = destinationAccountId;
        this.alias = alias;
        this.createdAt = createdAt;
    }

    /** Borrado lógico: una transferencia histórica debe conservar su beneficiario. */
    public void delete(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public UUID destinationAccountId() {
        return destinationAccountId;
    }

    public UUID id() {
        return id;
    }

    public String alias() {
        return alias;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
