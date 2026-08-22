package com.fincore.accounts.dto;

import com.fincore.accounts.entity.AccountStatus;

import jakarta.validation.constraints.NotNull;

/** Solicitud administrativa para cambiar el estado de una cuenta. */
public record ChangeAccountStatusRequest(@NotNull AccountStatus status) {
}
