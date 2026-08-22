package com.fincore.audit.repository;

import java.util.UUID;

import com.fincore.audit.entity.AuditEventEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
}
