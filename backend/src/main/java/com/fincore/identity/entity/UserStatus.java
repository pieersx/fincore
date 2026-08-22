package com.fincore.identity.entity;

/** Una cuenta suspendida conserva su historial, pero no puede autenticarse. */
public enum UserStatus {
    ACTIVE,
    SUSPENDED
}
