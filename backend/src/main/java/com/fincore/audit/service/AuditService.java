package com.fincore.audit.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.fincore.audit.dto.AuditEventView;
import com.fincore.audit.entity.AuditEventEntity;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.mapper.AuditEventMapper;
import com.fincore.audit.repository.AuditEventRepository;
import com.fincore.shared.dto.PageResponse;

import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso para registrar y consultar eventos de auditoría. */
@Service
public class AuditService {

    private final AuditEventRepository repository;
    private final AuditEventMapper mapper;
    private final Clock clock;

    AuditService(AuditEventRepository repository, AuditEventMapper mapper, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** Participa en la transacción de negocio existente o crea una cuando no existe. */
    @Transactional
    public void record(
            String actorUsername,
            String action,
            AuditOutcome outcome,
            String resourceType,
            String resourceId,
            String detail) {
        repository.save(new AuditEventEntity(
                UUID.randomUUID(),
                truncate(actorUsername, 50),
                truncate(action, 60),
                outcome,
                truncate(resourceType, 60),
                truncate(resourceId, 100),
                truncate(MDC.get("correlationId"), 100),
                truncate(detail, 500),
                Instant.now(clock)));
    }

    /** Ordena de forma descendente para mostrar primero la actividad más reciente. */
    @Transactional(readOnly = true)
    public PageResponse<AuditEventView> findAll(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        return PageResponse.from(repository.findAll(request), mapper::toView);
    }

    /** Evita que datos externos excedan el tamaño máximo de las columnas. */
    private String truncate(String value, int maximumLength) {
        if (value == null || value.length() <= maximumLength) {
            return value;
        }
        return value.substring(0, maximumLength);
    }
}
