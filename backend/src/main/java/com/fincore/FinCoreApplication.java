package com.fincore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de FinCore.
 *
 * <p>{@link SpringBootApplication} habilita el escaneo de componentes, la
 * autoconfiguración y la configuración principal de Spring Boot.
 */
@SpringBootApplication
public class FinCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinCoreApplication.class, args);
    }
}
