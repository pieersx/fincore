package com.fincore.identity.dto;

import java.util.List;
import java.util.UUID;

/** Información segura de la sesión autenticada. */
public record MeResponse(UUID id, String username, List<String> roles) {
}
