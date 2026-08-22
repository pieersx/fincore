package com.fincore.accounts.controller;

import java.util.List;
import java.util.UUID;

import com.fincore.accounts.dto.AccountView;
import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.ledger.dto.LedgerMovementView;
import com.fincore.shared.dto.PageResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultas del cliente autenticado sobre sus propias cuentas. */
@RestController
@RequestMapping("/api/v1/accounts")
@Validated
class AccountController {

    private final FinancialAccountService service;

    AccountController(FinancialAccountService service) {
        this.service = service;
    }

    @GetMapping
    List<AccountView> findOwnAccounts(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwnAccounts(user);
    }

    @GetMapping("/{accountId}")
    AccountView findOwnAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwnAccount(accountId, user);
    }

    @GetMapping("/{accountId}/movements")
    PageResponse<LedgerMovementView> findMovements(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwnMovements(accountId, user, page, size);
    }
}
