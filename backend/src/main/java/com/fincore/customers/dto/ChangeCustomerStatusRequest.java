package com.fincore.customers.dto;

import com.fincore.customers.entity.CustomerStatus;

import jakarta.validation.constraints.NotNull;

/** Solicitud administrativa para cambiar el estado de un cliente. */
public record ChangeCustomerStatusRequest(@NotNull CustomerStatus status) {
}
