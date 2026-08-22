package com.fincore.shared.exception;

/** Indica que el recurso solicitado no existe o no es visible para el usuario. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
