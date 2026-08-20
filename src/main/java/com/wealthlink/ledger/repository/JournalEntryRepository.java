package com.wealthlink.ledger.repository;

import com.wealthlink.ledger.entity.JournalEntry;
import com.wealthlink.ledger.entity.JournalEntryDirection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByJournalId(UUID journalId);
    List<JournalEntry> findByLedgerAccountId(UUID ledgerAccountId);
    List<JournalEntry> findByLedgerAccountIdAndDirection(UUID ledgerAccountId, JournalEntryDirection direction);
}
