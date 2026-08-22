package com.fincore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fincore.support.PostgreSQLIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FinCoreApplicationTests extends PostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void applicationContextStarts() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void healthEndpointReportsThatTheApplicationIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoEndpointIdentifiesTheApplication() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("FinCore"));
    }

    @Test
    void openApiPublishesTheFinancialContracts() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/accounts']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/transfers']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/operations/reconciliation']").exists());
    }

    @Test
    void flywayAppliesTheDatabaseBaseline() {
        Integer appliedMigrations = jdbcClient.sql("""
                        select count(*)
                        from flyway_schema_history
                        where success = true
                        """)
                .query(Integer.class)
                .single();

        assertThat(appliedMigrations).isEqualTo(6);
    }

    @Test
    void baseConfigurationDoesNotEnableDemoIdentitiesOrPublicRegistration() throws Exception {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        PropertySource<?> base = loader
                .load("application", new ClassPathResource("application.yml"))
                .getFirst();
        PropertySource<?> local = loader
                .load("application-local", new ClassPathResource("application-local.yml"))
                .getFirst();

        assertThat(base.getProperty("spring.flyway.locations")).isNull();
        assertThat(base.getProperty("fincore.identity.registration-enabled"))
                .isEqualTo("${REGISTRATION_ENABLED:false}");
        assertThat(local.getProperty("spring.flyway.locations"))
                .isEqualTo("classpath:db/migration,classpath:db/demo");
    }
}
