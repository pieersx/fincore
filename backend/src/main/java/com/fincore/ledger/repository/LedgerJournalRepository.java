package com.fincore.ledger.repository;

import java.util.UUID;

import com.fincore.ledger.entity.LedgerJournal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerJournalRepository extends JpaRepository<LedgerJournal, UUID> {
}
