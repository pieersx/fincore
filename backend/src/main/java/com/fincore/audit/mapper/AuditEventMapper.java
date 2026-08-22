package com.fincore.audit.mapper;

import com.fincore.audit.dto.AuditEventView;
import com.fincore.audit.entity.AuditEventEntity;

import org.springframework.stereotype.Component;

/** Separa la entidad de auditoría del DTO expuesto para consultas. */
@Component
public class AuditEventMapper {

    public AuditEventView toView(AuditEventEntity event) {
        return new AuditEventView(
                event.id(),
                event.actorUsername(),
                event.action(),
                event.outcome(),
                event.resourceType(),
                event.resourceId(),
                event.correlationId(),
                event.detail(),
                event.occurredAt());
    }
}
