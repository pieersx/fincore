package com.fincore.audit.service;

import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
class AuthenticationAuditListener {

    private final AuditService auditService;

    AuthenticationAuditListener(AuditService auditService) {
        this.auditService = auditService;
    }

    /** Spring publica este evento después de validar correctamente las credenciales. */
    @EventListener
    void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditService.record(
                event.getAuthentication().getName(),
                "AUTH_LOGIN",
                AuditOutcome.SUCCESS,
                "SESSION",
                null,
                "Inicio de sesión correcto");
    }

    /** Registra el intento fallido sin guardar la contraseña recibida. */
    @EventListener
    void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        auditService.record(
                event.getAuthentication().getName(),
                "AUTH_LOGIN",
                AuditOutcome.FAILURE,
                "SESSION",
                null,
                "Credenciales inválidas");
    }

    @EventListener
    void onLogoutSuccess(LogoutSuccessEvent event) {
        if (event.getAuthentication() == null) {
            return;
        }
        auditService.record(
                event.getAuthentication().getName(),
                "AUTH_LOGOUT",
                AuditOutcome.SUCCESS,
                "SESSION",
                null,
                "Cierre de sesión correcto");
    }
}
