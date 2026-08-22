package com.fincore.accounts.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fincore.accounts.entity.FinancialAccount;
import com.fincore.shared.model.Currency;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {

    List<FinancialAccount> findByCustomerIdOrderByCurrency(UUID customerId);

    Page<FinancialAccount> findByCustomerId(UUID customerId, Pageable pageable);

    Optional<FinancialAccount> findByCustomerIdAndCurrency(UUID customerId, Currency currency);

    Optional<FinancialAccount> findByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    /** El bloqueo pesimista serializa transferencias que compiten por la misma cuenta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from FinancialAccount account where account.id = :accountId")
    Optional<FinancialAccount> findByIdForUpdate(@Param("accountId") UUID accountId);
}
