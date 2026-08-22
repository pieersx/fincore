package com.fincore;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fincore.support.PostgreSQLIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/reset-identity-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class IdentityCustomerIntegrationTests extends PostgreSQLIntegrationTest {

    private static final String DEMO_PASSWORD = "FincoreDemo!2026";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void csrfEndpointCreatesATokenForTheBrowserSession() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));
    }

    @Test
    void anonymousRequestsCannotReadACustomerProfile() throws Exception {
        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void customerCanLogInAndReadOnlyTheProfileLinkedToTheSession() throws Exception {
        MockHttpSession session = login("customer.one", DEMO_PASSWORD);

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("customer.one"))
                .andExpect(jsonPath("$.roles", hasItem("CUSTOMER")));

        mockMvc.perform(get("/api/v1/customers/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("20000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.displayName").value("Cliente Demo Uno"));
    }

    @Test
    void customerCannotUseAdministrativeEndpoints() throws Exception {
        MockHttpSession session = login("customer.one", DEMO_PASSWORD);

        mockMvc.perform(get("/api/v1/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
    }

    @Test
    void administratorCanListUsersAndCustomers() throws Exception {
        MockHttpSession session = login("admin.demo", DEMO_PASSWORD);

        mockMvc.perform(get("/api/v1/admin/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].username", hasItem("customer.one")));

        mockMvc.perform(get("/api/v1/admin/customers").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void analystCanReadAuditButCannotAdministerUsers() throws Exception {
        MockHttpSession session = login("analyst.demo", DEMO_PASSWORD);

        mockMvc.perform(get("/api/v1/audit-events").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].action", hasItem("AUTH_LOGIN")));

        mockMvc.perform(get("/api/v1/admin/users").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationCreatesUserAndCustomerAndRejectsDuplicates() throws Exception {
        String request = """
                {
                  "username": "new.customer",
                  "password": "AnotherDemo!2026",
                  "displayName": "Nuevo Cliente Sintético"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value("new.customer"))
                .andExpect(jsonPath("$.user.roles", hasItem("CUSTOMER")))
                .andExpect(jsonPath("$.customer.displayName").value("Nuevo Cliente Sintético"))
                .andExpect(jsonPath("$.accounts.length()").value(2))
                .andExpect(jsonPath("$.accounts[*].currency", hasItem("PEN")))
                .andExpect(jsonPath("$.accounts[*].currency", hasItem("USD")));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto"));
    }

    @Test
    void logoutRequiresCsrfAndInvalidatesTheSession() throws Exception {
        MockHttpSession session = login("customer.two", DEMO_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithoutAnAuthenticatedSessionIsIdempotent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void administratorCannotSuspendItsOwnAccount() throws Exception {
        MockHttpSession session = login("admin.demo", DEMO_PASSWORD);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status",
                        "10000000-0000-0000-0000-000000000004")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Operación no permitida"));
    }

    @Test
    void suspendedUserCannotStartANewSession() throws Exception {
        MockHttpSession administrator = login("admin.demo", DEMO_PASSWORD);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status",
                        "10000000-0000-0000-0000-000000000001")
                        .session(administrator)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .param("username", "customer.one")
                        .param("password", DEMO_PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsPasswordsThatExceedTheBcryptByteLimit() throws Exception {
        String oversizedPassword = "😀".repeat(30);
        String request = """
                {
                  "username": "unicode.customer",
                  "password": "%s",
                  "displayName": "Cliente Unicode Sintético"
                }
                """.formatted(oversizedPassword);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Operación no permitida"));
    }

    @Test
    void registrationRejectsADisplayNameWithOnlyOneVisibleCharacter() throws Exception {
        String request = """
                {
                  "username": "short.name",
                  "password": "AnotherDemo!2026",
                  "displayName": " A "
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.displayName")
                        .value("debe contener al menos 2 caracteres visibles"));
    }

    /** Ejecuta el mismo formulario que utilizará React y devuelve su sesión autenticada. */
    private MockHttpSession login(String username, String password) throws Exception {
        MockHttpSession initialSession = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .session(initialSession)
                        .with(csrf())
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isNoContent())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
