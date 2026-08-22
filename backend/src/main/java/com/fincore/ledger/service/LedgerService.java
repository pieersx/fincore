package com.fincore.ledger.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fincore.ledger.entity.LedgerEntryType;
import com.fincore.ledger.dto.LedgerMovementView;
import com.fincore.ledger.entity.LedgerReferenceType;
import com.fincore.ledger.entity.LedgerEntry;
import com.fincore.ledger.entity.LedgerJournal;
import com.fincore.ledger.repository.LedgerEntryRepository;
import com.fincore.ledger.repository.LedgerJournalRepository;
import com.fincore.shared.dto.PageResponse;
import com.fincore.shared.model.Currency;

import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerJournalRepository journalRepository;
    private final LedgerEntryRepository entryRepository;
    private final JdbcClient jdbcClient;

    LedgerService(
            LedgerJournalRepository journalRepository,
            LedgerEntryRepository entryRepository,
            JdbcClient jdbcClient) {
        this.journalRepository = journalRepository;
        this.entryRepository = entryRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Crea el journal y sus dos lados. El trigger diferido de PostgreSQL vuelve a comprobar
     * en el commit que la suma de débitos sea exactamente igual a la de créditos.
     */
    @Transactional
    public UUID recordTransfer(
            UUID transferId,
            Currency currency,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String description,
            String actorUsername,
            Instant occurredAt) {
        LedgerJournal journal = journalRepository.save(new LedgerJournal(
                UUID.randomUUID(),
                LedgerReferenceType.TRANSFER,
                transferId,
                currency,
                description,
                actorUsername,
                occurredAt));
        entryRepository.save(new LedgerEntry(
                UUID.randomUUID(), journal, sourceAccountId, LedgerEntryType.DEBIT, amount, occurredAt));
        entryRepository.save(new LedgerEntry(
                UUID.randomUUID(), journal, destinationAccountId, LedgerEntryType.CREDIT, amount, occurredAt));
        return journal.id();
    }

    @Transactional(readOnly = true)
    public PageResponse<LedgerMovementView> findMovements(UUID accountId, int page, int size) {
        return PageResponse.from(
                entryRepository.findMovements(accountId, PageRequest.of(page, size)),
                movement -> movement);
    }

    /** Reconstruye saldos solo con entradas inmutables; es la base de la conciliación. */
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> reconstructBalances() {
        return jdbcClient.sql("""
                        select account_id,
                               sum(case when entry_type = 'CREDIT' then amount else -amount end) as balance
                        from ledger_entry
                        group by account_id
                        """)
                .query((resultSet, rowNumber) -> new BalanceRow(
                        resultSet.getObject("account_id", UUID.class),
                        resultSet.getBigDecimal("balance")))
                .list()
                .stream()
                .collect(Collectors.toUnmodifiableMap(BalanceRow::accountId, BalanceRow::balance));
    }

    private record BalanceRow(UUID accountId, BigDecimal balance) {
    }
}
