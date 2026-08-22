package com.fincore.onboarding.controller;

import java.net.URI;
import com.fincore.onboarding.dto.RegistrationRequest;
import com.fincore.onboarding.dto.RegistrationResponse;
import com.fincore.onboarding.service.CustomerOnboardingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone el alta coordinada sin mezclar las entidades internas de otras funcionalidades. */
@RestController
@RequestMapping("/api/v1/auth")
class RegistrationController {

    private final CustomerOnboardingService service;

    RegistrationController(CustomerOnboardingService service) {
        this.service = service;
    }

    @PostMapping("/register")
    ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        CustomerOnboardingService.RegisteredCustomer result = service.register(
                request.username(),
                request.password(),
                request.displayName());
        return ResponseEntity
                .created(URI.create("/api/v1/customers/me"))
                .body(new RegistrationResponse(result.user(), result.customer(), result.accounts()));
    }
}
