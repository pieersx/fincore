package com.fincore.identity.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.fincore.identity.entity.Role;
import com.fincore.identity.entity.UserStatus;

/** Proyección segura de un usuario; nunca contiene el hash de la contraseña. */
public record UserView(
        UUID id,
        String username,
        UserStatus status,
        Set<Role> roles,
        Instant createdAt,
        Instant updatedAt) {
}
