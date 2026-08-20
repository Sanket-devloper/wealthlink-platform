package com.wealthlink.ledger.repository;

import com.wealthlink.ledger.entity.Journal;
import com.wealthlink.ledger.entity.JournalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalRepository extends JpaRepository<Journal, UUID> {
    Optional<Journal> findByJournalReference(String journalReference);
    List<Journal> findByStatus(JournalStatus status);
    List<Journal> findByJournalDateBetween(LocalDate from, LocalDate to);
    List<Journal> findByReferenceId(UUID referenceId);
    Optional<Journal> findByIdempotencyKey(String idempotencyKey);
}
