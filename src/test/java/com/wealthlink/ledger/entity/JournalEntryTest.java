package com.wealthlink.ledger.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JournalEntry entity lifecycle callback (@PrePersist).
 * No Spring context or database required.
 */
class JournalEntryTest {

    @Test
    void onCreate_setsCreatedAt_whenNotSet() {
        JournalEntry entry = JournalEntry.builder()
                .direction(JournalEntryDirection.DEBIT)
                .build();

        entry.onCreate();

        assertThat(entry.getCreatedAt()).isNotNull();
    }

    @Test
    void onCreate_doesNotOverrideExistingCreatedAt() {
        Instant fixedTime = Instant.parse("2024-01-15T10:00:00Z");
        JournalEntry entry = JournalEntry.builder()
                .direction(JournalEntryDirection.CREDIT)
                .createdAt(fixedTime)
                .build();

        entry.onCreate();

        assertThat(entry.getCreatedAt()).isEqualTo(fixedTime);
    }
}
