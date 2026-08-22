package com.fincore.identity.dto;

/** Datos que el frontend necesita para enviar el token CSRF. */
public record CsrfResponse(String token, String headerName, String parameterName) {
}
