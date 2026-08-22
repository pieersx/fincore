package com.fincore.identity.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.identity.entity.Role;
import com.fincore.identity.entity.UserStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "app_user")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected UserAccount() {
    }

    public UserAccount(
            UUID id,
            String username,
            String passwordHash,
            UserStatus status,
            Set<Role> roles,
            Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles.addAll(roles);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public UserStatus status() {
        return status;
    }

    public Set<Role> roles() {
        return Set.copyOf(roles);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    /** Cambiar el estado conserva la cuenta y toda su trazabilidad. */
    public void changeStatus(UserStatus newStatus, Instant changedAt) {
        status = newStatus;
        updatedAt = changedAt;
    }

    public AuthenticatedUser toPrincipal() {
        return new AuthenticatedUser(
                id,
                username,
                passwordHash,
                roles,
                status == UserStatus.ACTIVE);
    }

}
