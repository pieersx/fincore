package com.fincore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fincore.support.PostgreSQLIntegrationTest;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FinancialIntegrationTests extends PostgreSQLIntegrationTest {

    private static final String DEMO_PASSWORD = "FincoreDemo!2026";
    private static final String CUSTOMER_ONE_PEN = "30000000-0000-0000-0000-000000000001";
    private static final String CUSTOMER_TWO_PEN = "30000000-0000-0000-0000-000000000003";
    private static final String CUSTOMER_TWO_BENEFICIARY = "60000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void customerCanReadOnlyOwnAccountsMovementsAndBeneficiaries() throws Exception {
        MockHttpSession session = login("customer.one");

        mockMvc.perform(get("/api/v1/accounts").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].currency", hasItem("PEN")))
                .andExpect(jsonPath("$[*].currency", hasItem("USD")));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/movements", CUSTOMER_ONE_PEN).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content[*].referenceType", hasItem("OPENING_BALANCE")));

        mockMvc.perform(get("/api/v1/beneficiaries").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].destinationAccountNumber", hasItem("FCPEN0000000002")));

        mockMvc.perform(get("/api/v1/accounts/{accountId}", CUSTOMER_TWO_PEN).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferIsAtomicIdempotentAndProducesAPdfReceipt() throws Exception {
        MockHttpSession session = login("customer.one");
        BigDecimal sourceBefore = balance(CUSTOMER_ONE_PEN);
        BigDecimal destinationBefore = balance(CUSTOMER_TWO_PEN);
        String key = "integration-transfer-0001";
        String request = transferRequest("10.25", "Prueba de idempotencia");

        MvcResult first = mockMvc.perform(post("/api/v1/transfers")
                        .session(session)
                        .with(csrf())
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();
        String transferId = JsonPath.read(first.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");

        MvcResult repeated = mockMvc.perform(post("/api/v1/transfers")
                        .session(session)
                        .with(csrf())
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();
        assertThat((String) JsonPath.read(
                repeated.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id"))
                .isEqualTo(transferId);

        assertThat(balance(CUSTOMER_ONE_PEN)).isEqualByComparingTo(sourceBefore.subtract(new BigDecimal("10.25")));
        assertThat(balance(CUSTOMER_TWO_PEN)).isEqualByComparingTo(destinationBefore.add(new BigDecimal("10.25")));
        assertThat(count("select count(*) from financial_transfer where id = cast(? as uuid)", transferId)).isEqualTo(1);
        assertThat(count("select count(*) from ledger_entry where journal_id = "
                + "(select id from ledger_journal where reference_id = cast(? as uuid))", transferId)).isEqualTo(2);

        mockMvc.perform(post("/api/v1/transfers")
                        .session(session)
                        .with(csrf())
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferRequest("11.00", "Solicitud diferente")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto"));

        mockMvc.perform(get("/api/v1/transfers/{transferId}/receipt", transferId).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes()));
    }

    @Test
    void concurrentRequestsWithTheSameKeyCreateOnlyOneTransfer() throws Exception {
        MockHttpSession firstSession = login("customer.one");
        MockHttpSession secondSession = login("customer.one");
        String key = "concurrent-transfer-0001";
        String request = transferRequest("1.00", "Concurrencia");

        CompletableFuture<MvcResult> first = CompletableFuture.supplyAsync(
                () -> performTransfer(firstSession, key, request));
        CompletableFuture<MvcResult> second = CompletableFuture.supplyAsync(
                () -> performTransfer(secondSession, key, request));
        List<MvcResult> results = CompletableFuture.allOf(first, second)
                .thenApply(ignored -> List.of(first.join(), second.join()))
                .get(20, TimeUnit.SECONDS);

        List<String> ids = results.stream()
                .map(result -> (String) JsonPath.read(
                        new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8), "$.id"))
                .distinct()
                .toList();
        assertThat(results).allSatisfy(result -> assertThat(result.getResponse().getStatus()).isEqualTo(201));
        assertThat(ids).hasSize(1);
        assertThat(count("select count(*) from transfer_idempotency where idempotency_key = ?", key)).isEqualTo(1);
        assertThat(count("select count(*) from financial_transfer where id = cast(? as uuid)", ids.getFirst()))
                .isEqualTo(1);
    }

    @Test
    void analystCanReconcileAndInspectAllTransfers() throws Exception {
        MockHttpSession session = login("analyst.demo");

        mockMvc.perform(get("/api/v1/operations/reconciliation").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true))
                .andExpect(jsonPath("$.mismatchCount").value(0));

        mockMvc.perform(get("/api/v1/operations/transfers").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void confirmedTransfersAndLedgerEntriesAreImmutableInPostgreSQL() {
        assertThatThrownBy(() -> jdbcClient.sql("""
                        update financial_transfer
                        set description = 'Mutación no permitida'
                        where id = '70000000-0000-0000-0000-000000000001'
                        """).update())
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbcClient.sql("""
                        update ledger_entry
                        set amount = 999.00
                        where id = '50000000-0000-0000-0000-000000000001'
                        """).update())
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void transferWithInsufficientFundsRollsBackEverything() throws Exception {
        MockHttpSession session = login("customer.one");
        BigDecimal sourceBefore = balance(CUSTOMER_ONE_PEN);
        int transfersBefore = count("select count(*) from financial_transfer", null);

        mockMvc.perform(post("/api/v1/transfers")
                        .session(session)
                        .with(csrf())
                        .header("Idempotency-Key", "insufficient-funds-0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferRequest("99999.00", "Debe fallar")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Operación no permitida"));

        assertThat(balance(CUSTOMER_ONE_PEN)).isEqualByComparingTo(sourceBefore);
        assertThat(count("select count(*) from financial_transfer", null)).isEqualTo(transfersBefore);
        assertThat(count("select count(*) from transfer_idempotency where idempotency_key = ?",
                "insufficient-funds-0001")).isZero();
    }

    private MvcResult performTransfer(MockHttpSession session, String key, String request) {
        try {
            return mockMvc.perform(post("/api/v1/transfers")
                            .session(session)
                            .with(csrf())
                            .header("Idempotency-Key", key)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andReturn();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String transferRequest(String amount, String description) {
        return """
                {
                  "sourceAccountId": "%s",
                  "beneficiaryId": "%s",
                  "amount": %s,
                  "description": "%s"
                }
                """.formatted(CUSTOMER_ONE_PEN, CUSTOMER_TWO_BENEFICIARY, amount, description);
    }

    private BigDecimal balance(String accountId) {
        return jdbcClient.sql("select balance from financial_account where id = cast(:id as uuid)")
                .param("id", accountId)
                .query(BigDecimal.class)
                .single();
    }

    private int count(String sql, String parameter) {
        JdbcClient.StatementSpec statement = jdbcClient.sql(sql);
        if (parameter != null) {
            statement = statement.param(parameter);
        }
        return statement.query(Integer.class).single();
    }

    private MockHttpSession login(String username) throws Exception {
        MockHttpSession initialSession = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .session(initialSession)
                        .with(csrf())
                        .param("username", username)
                        .param("password", DEMO_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
