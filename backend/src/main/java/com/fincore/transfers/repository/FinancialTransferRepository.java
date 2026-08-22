package com.fincore.transfers.repository;

import java.util.Optional;
import java.util.UUID;

import com.fincore.transfers.entity.FinancialTransfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialTransferRepository extends JpaRepository<FinancialTransfer, UUID> {

    Page<FinancialTransfer> findByCreatedByUserId(UUID userId, Pageable pageable);

    Optional<FinancialTransfer> findByIdAndCreatedByUserId(UUID transferId, UUID userId);
}
