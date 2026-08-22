package com.fincore.customers.controller;

import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.customers.service.CustomerProfileService;
import com.fincore.shared.security.AuthenticatedUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Recursos que un cliente puede consultar solamente sobre sí mismo. */
@RestController
@RequestMapping("/api/v1/customers")
class CustomerSelfController {

    private final CustomerProfileService service;

    CustomerSelfController(CustomerProfileService service) {
        this.service = service;
    }

    @GetMapping("/me")
    CustomerProfileView findOwnProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwnProfile(user);
    }
}
