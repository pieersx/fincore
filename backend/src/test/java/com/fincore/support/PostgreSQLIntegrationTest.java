package com.fincore.support;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Comparte un único PostgreSQL real entre las pruebas de integración del proceso Maven.
 */
@ActiveProfiles("test")
public abstract class PostgreSQLIntegrationTest {

    private static final String EXTERNAL_URL = System.getenv("FINCORE_TEST_DB_URL");
    protected static final PostgreSQLContainer POSTGRES;

    /*
     * Se inicia manualmente una sola vez. La extensión JUnit detendría el mismo
     * contenedor al finalizar la primera clase y rompería el contexto compartido.
     */
    static {
        if (EXTERNAL_URL == null || EXTERNAL_URL.isBlank()) {
            POSTGRES = new PostgreSQLContainer("postgres:18.6-alpine")
                    .withDatabaseName("fincore_test")
                    .withUsername("fincore")
                    .withPassword("fincore_test");
            POSTGRES.start();
        } else {
            // Alternativa útil cuando PostgreSQL se ejecuta fuera del daemon visible por Testcontainers.
            POSTGRES = null;
        }
    }

    /**
     * Entrega a Spring las credenciales y la URL asignadas dinámicamente por Docker.
     */
    @DynamicPropertySource
    static void configurePostgreSQL(DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
            return;
        }
        registry.add("spring.datasource.url", () -> EXTERNAL_URL);
        registry.add("spring.datasource.username", () -> environment("FINCORE_TEST_DB_USERNAME", "fincore"));
        registry.add("spring.datasource.password", () -> environment("FINCORE_TEST_DB_PASSWORD", "fincore_test"));
    }

    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
