package com.fincore.shared.model;

/**
 * Monedas soportadas por la aplicación. Vive en shared para evitar dependencias circulares
 * entre cuentas, ledger y transferencias.
 */
public enum Currency {
    PEN,
    USD
}
