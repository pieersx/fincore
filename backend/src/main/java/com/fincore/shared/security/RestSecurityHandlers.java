package com.fincore.shared.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/** Respuestas JSON de seguridad para que una SPA no reciba redirecciones HTML. */
@Component
class RestSecurityHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestSecurityHandlers.class);
    private final AuditService auditService;

    RestSecurityHandlers(AuditService auditService) {
        this.auditService = auditService;
    }

    AuthenticationEntryPoint authenticationEntryPoint() {
        return this::writeUnauthorized;
    }

    AuthenticationFailureHandler authenticationFailureHandler() {
        return this::writeLoginFailure;
    }

    AccessDeniedHandler accessDeniedHandler() {
        return this::writeForbidden;
    }

    private void writeUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        writeProblem(response, 401, "No autenticado", "Debes iniciar sesión para acceder al recurso.");
    }

    private void writeLoginFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        writeProblem(response, 401, "Credenciales inválidas", "El usuario o la contraseña no son válidos.");
    }

    /** Registra la denegación sin guardar parámetros ni cuerpos potencialmente sensibles. */
    private void writeForbidden(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception) throws IOException {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        String actor = authentication == null ? "anonymous" : authentication.getName();
        try {
            auditService.record(
                    actor,
                    "ACCESS_DENIED",
                    AuditOutcome.DENIED,
                    "HTTP_REQUEST",
                    request.getRequestURI(),
                    "Método: " + request.getMethod());
        } catch (RuntimeException auditFailure) {
            // La autorización ya fue denegada; un fallo de auditoría no debe convertirla en un error 500.
            LOGGER.error("No se pudo persistir la denegación de acceso", auditFailure);
        }
        writeProblem(response, 403, "Acceso denegado", "No tienes permiso o falta un token CSRF válido.");
    }

    private void writeProblem(HttpServletResponse response, int status, String title, String detail)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank","title":"%s","status":%d,"detail":"%s"}
                """.formatted(title, status, detail).trim());
    }
}
