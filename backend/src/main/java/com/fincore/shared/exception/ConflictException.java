package com.fincore.shared.exception;

/** Indica que la solicitud entra en conflicto con el estado actual del sistema. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
