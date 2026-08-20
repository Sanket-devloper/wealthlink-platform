package com.wealthlink.ledger.service;

import com.wealthlink.ledger.dto.CreateJournalRequest;
import com.wealthlink.ledger.dto.JournalResponse;
import com.wealthlink.ledger.dto.ReverseJournalRequest;
import com.wealthlink.ledger.dto.ReverseJournalResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Journal (double-entry bookkeeping) operations.
 */
public interface JournalService {

    /** Retrieve all journals. */
    List<JournalResponse> getAll();

    /** Retrieve a single journal by its primary key. */
    JournalResponse getById(UUID id);

    /**
     * Create and post a new journal.
     * Validates double-entry balance (sum of DEBITs == sum of CREDITs per currency).
     * Supports idempotency via {@code request.idempotencyKey}.
     */
    JournalResponse createJournal(CreateJournalRequest request);

    /**
     * Reverse a previously posted journal.
     * Creates a mirror journal with inverted DEBIT/CREDIT directions and links them.
     * The original journal is marked REVERSED.
     */
    ReverseJournalResponse reverseJournal(UUID id, ReverseJournalRequest request);
}
