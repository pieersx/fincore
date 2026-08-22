package com.fincore.shared.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class TimeConfiguration {

    /** Centraliza el reloj para que las pruebas futuras puedan sustituirlo. */
    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }
}
