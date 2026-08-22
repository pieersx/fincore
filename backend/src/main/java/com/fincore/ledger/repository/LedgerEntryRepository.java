package com.fincore.ledger.repository;

import java.util.UUID;

import com.fincore.ledger.dto.LedgerMovementView;
import com.fincore.ledger.entity.LedgerEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query(
            value = """
                    select new com.fincore.ledger.dto.LedgerMovementView(
                        entry.id, journal.id, journal.referenceType, journal.referenceId,
                        entry.entryType, entry.amount, journal.currency, journal.description,
                        journal.occurredAt)
                    from LedgerEntry entry join entry.journal journal
                    where entry.accountId = :accountId
                    order by journal.occurredAt desc, entry.id desc
                    """,
            countQuery = "select count(entry) from LedgerEntry entry where entry.accountId = :accountId")
    Page<LedgerMovementView> findMovements(@Param("accountId") UUID accountId, Pageable pageable);
}
