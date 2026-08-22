package com.fincore.transfers.controller;

import java.util.UUID;

import com.fincore.shared.dto.PageResponse;
import com.fincore.transfers.dto.TransferView;
import com.fincore.transfers.service.TransferService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Lectura global para investigación operativa; nunca modifica saldos. */
@RestController
@RequestMapping("/api/v1/operations/transfers")
@Validated
class TransferOperationsController {

    private final TransferService service;

    TransferOperationsController(TransferService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<TransferView> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAll(page, size);
    }

    @GetMapping("/{transferId}")
    TransferView findById(@PathVariable UUID transferId) {
        return service.findById(transferId);
    }
}
