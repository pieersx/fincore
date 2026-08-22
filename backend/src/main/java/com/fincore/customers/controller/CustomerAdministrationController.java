package com.fincore.customers.controller;

import java.util.UUID;

import com.fincore.customers.dto.ChangeCustomerStatusRequest;
import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.customers.entity.CustomerStatus;
import com.fincore.customers.service.CustomerProfileService;
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

/** Endpoints administrativos protegidos por el rol ADMIN en SecurityConfiguration. */
@RestController
@RequestMapping("/api/v1/admin/customers")
@Validated
class CustomerAdministrationController {

    private final CustomerProfileService service;

    CustomerAdministrationController(CustomerProfileService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<CustomerProfileView> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAll(page, size);
    }

    @GetMapping("/{customerId}")
    CustomerProfileView findById(@PathVariable UUID customerId) {
        return service.findById(customerId);
    }

    @PatchMapping("/{customerId}/status")
    CustomerProfileView changeStatus(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChangeCustomerStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser administrator) {
        return service.changeStatus(customerId, request.status(), administrator);
    }
}
