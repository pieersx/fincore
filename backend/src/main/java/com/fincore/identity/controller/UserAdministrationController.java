package com.fincore.identity.controller;

import java.util.UUID;

import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.identity.dto.ChangeUserStatusRequest;
import com.fincore.identity.entity.UserStatus;
import com.fincore.identity.dto.UserView;
import com.fincore.identity.service.UserAdministrationService;
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

/** Operaciones administrativas separadas de los recursos disponibles para clientes. */
@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
class UserAdministrationController {

    private final UserAdministrationService service;

    UserAdministrationController(UserAdministrationService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<UserView> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.findAll(page, size);
    }

    @PatchMapping("/{userId}/status")
    UserView changeStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser administrator) {
        return service.changeStatus(userId, request.status(), administrator);
    }
}
