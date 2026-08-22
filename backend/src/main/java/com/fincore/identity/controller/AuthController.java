package com.fincore.identity.controller;

import java.util.List;
import java.util.Objects;

import com.fincore.identity.dto.CsrfResponse;
import com.fincore.identity.dto.MeResponse;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.identity.entity.Role;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contratos HTTP relacionados con registro, sesión actual y protección CSRF. */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    /**
     * Fuerza la creación del token almacenado en sesión y comunica cómo enviarlo.
     */
    @GetMapping("/csrf")
    CsrfResponse csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        CsrfToken requiredToken = Objects.requireNonNull(token, "Spring Security debe crear el token CSRF");
        return new CsrfResponse(
                requiredToken.getToken(),
                requiredToken.getHeaderName(),
                requiredToken.getParameterName());
    }

    /** Devuelve la identidad de la sesión sin exponer credenciales. */
    @GetMapping("/me")
    MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        List<String> roles = user.roles().stream().map(Role::name).sorted().toList();
        return new MeResponse(user.id(), user.getUsername(), roles);
    }

}
