package com.fincore.audit.controller;

import com.fincore.audit.dto.AuditEventView;
import com.fincore.audit.service.AuditService;
import com.fincore.shared.dto.PageResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de consulta; los eventos de auditoría son inmutables desde la API. */
@RestController
@RequestMapping("/api/v1/audit-events")
@Validated
class AuditController {

    private final AuditService service;

    AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<AuditEventView> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAll(page, size);
    }
}
