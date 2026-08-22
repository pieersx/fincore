package com.fincore.identity.dto;

import com.fincore.identity.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

/** Solicitud administrativa para activar o suspender un usuario. */
public record ChangeUserStatusRequest(@NotNull UserStatus status) {
}
