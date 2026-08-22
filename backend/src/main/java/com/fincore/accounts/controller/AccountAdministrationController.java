package com.fincore.accounts.controller;

import java.util.UUID;

import com.fincore.accounts.dto.ChangeAccountStatusRequest;
import com.fincore.accounts.entity.AccountStatus;
import com.fincore.accounts.dto.AccountView;
import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.shared.dto.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@Validated
class AccountAdministrationController {

    private final FinancialAccountService service;

    AccountAdministrationController(FinancialAccountService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<AccountView> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAll(page, size);
    }

    @GetMapping("/{accountId}")
    AccountView findById(@PathVariable UUID accountId) {
        return service.findById(accountId);
    }

    @PatchMapping("/{accountId}/status")
    AccountView changeStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeAccountStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser administrator) {
        return service.changeStatus(accountId, request.status(), administrator);
    }
}
