package com.fincore.customers.entity;

/** Estado del perfil; las funcionalidades financieras validarán ACTIVE antes de operar. */
public enum CustomerStatus {
    ACTIVE,
    SUSPENDED
}
