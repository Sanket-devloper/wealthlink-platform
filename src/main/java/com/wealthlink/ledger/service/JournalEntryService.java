package com.wealthlink.ledger.service;

import com.wealthlink.ledger.dto.JournalResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for individual JournalEntry operations.
 * Entries are created as part of a Journal; this service exposes read access.
 */
public interface JournalEntryService {

    /** Retrieve all journal entries across all journals. */
    List<JournalResponse.JournalEntryResponse> getAll();

    /** Retrieve a single journal entry by its primary key. */
    JournalResponse.JournalEntryResponse getById(UUID id);
}
