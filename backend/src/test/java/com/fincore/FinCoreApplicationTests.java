package com.fincore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FinCoreApplicationTests {

    @Autowired
    private MockMvc mockMvc;

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
    void moduleStructureIsValid() {
        ApplicationModules.of(FinCoreApplication.class).verify();
    }
}
