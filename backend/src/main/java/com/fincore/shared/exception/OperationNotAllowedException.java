package com.fincore.shared.exception;

/** Indica que una regla de negocio impide completar la operación solicitada. */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
