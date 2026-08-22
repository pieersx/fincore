package com.fincore.customers.repository;

import java.util.Optional;
import java.util.UUID;

import com.fincore.customers.entity.CustomerProfile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {

    Optional<CustomerProfile> findByUserId(UUID userId);
}
