package com.fincore.beneficiaries.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fincore.beneficiaries.entity.Beneficiary;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByOwnerCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID ownerCustomerId);

    Optional<Beneficiary> findByIdAndOwnerCustomerIdAndDeletedAtIsNull(UUID id, UUID ownerCustomerId);

    boolean existsByOwnerCustomerIdAndDestinationAccountIdAndDeletedAtIsNull(
            UUID ownerCustomerId,
            UUID destinationAccountId);
}
