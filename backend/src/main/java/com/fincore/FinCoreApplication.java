package com.fincore;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;

/**
 * FinCore application entry point.
 *
 * <p>{@link Modulith} marks the direct subpackages of {@code com.fincore} as business modules.
 */
@Modulith(systemName = "FinCore")
public class FinCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinCoreApplication.class, args);
    }
}
