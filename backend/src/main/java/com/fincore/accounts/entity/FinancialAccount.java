package com.fincore.accounts.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fincore.accounts.entity.AccountKind;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.model.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "financial_account")
public class FinancialAccount {

    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_kind", nullable = false, length = 20)
    private AccountKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected FinancialAccount() {
    }

    public FinancialAccount(
            UUID id,
            UUID customerId,
            String accountNumber,
            AccountKind kind,
            Currency currency,
            BigDecimal balance,
            Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.kind = kind;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /** Aplica el débito después de verificar estado y fondos disponibles. */
    public void debit(BigDecimal amount, Instant changedAt) {
        requireActive();
        if (kind == AccountKind.CUSTOMER && balance.compareTo(amount) < 0) {
            throw new OperationNotAllowedException("La cuenta de origen no tiene saldo suficiente.");
        }
        balance = balance.subtract(amount);
        updatedAt = changedAt;
    }

    /** Acreditar también exige que la cuenta destino esté habilitada. */
    public void credit(BigDecimal amount, Instant changedAt) {
        requireActive();
        balance = balance.add(amount);
        updatedAt = changedAt;
    }

    public void changeStatus(AccountStatus newStatus, Instant changedAt) {
        status = newStatus;
        updatedAt = changedAt;
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public String accountNumber() {
        return accountNumber;
    }

    public AccountKind kind() {
        return kind;
    }

    public Currency currency() {
        return currency;
    }

    public BigDecimal balance() {
        return balance;
    }

    public AccountStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new OperationNotAllowedException("La cuenta está suspendida.");
        }
    }
}
