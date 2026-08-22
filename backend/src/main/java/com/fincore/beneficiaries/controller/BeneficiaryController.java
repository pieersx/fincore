package com.fincore.beneficiaries.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.fincore.beneficiaries.dto.BeneficiaryView;
import com.fincore.beneficiaries.dto.CreateBeneficiaryRequest;
import com.fincore.beneficiaries.service.BeneficiaryService;
import com.fincore.shared.security.AuthenticatedUser;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beneficiaries")
class BeneficiaryController {

    private final BeneficiaryService service;

    BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    @GetMapping
    List<BeneficiaryView> findOwn(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwn(user);
    }

    @PostMapping
    ResponseEntity<BeneficiaryView> create(
            @Valid @RequestBody CreateBeneficiaryRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BeneficiaryView beneficiary = service.create(request.destinationAccountNumber(), request.alias(), user);
        return ResponseEntity
                .created(URI.create("/api/v1/beneficiaries/" + beneficiary.id()))
                .body(beneficiary);
    }

    @DeleteMapping("/{beneficiaryId}")
    ResponseEntity<Void> delete(
            @PathVariable UUID beneficiaryId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.delete(beneficiaryId, user);
        return ResponseEntity.noContent().build();
    }
}
