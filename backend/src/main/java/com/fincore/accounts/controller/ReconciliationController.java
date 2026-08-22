package com.fincore.accounts.controller;

import com.fincore.accounts.dto.ReconciliationView;
import com.fincore.accounts.service.FinancialAccountService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Diagnóstico operativo disponible solo para ANALYST y ADMIN. */
@RestController
@RequestMapping("/api/v1/operations/reconciliation")
class ReconciliationController {

    private final FinancialAccountService service;

    ReconciliationController(FinancialAccountService service) {
        this.service = service;
    }

    @GetMapping
    ReconciliationView reconcile() {
        return service.reconcile();
    }
}
