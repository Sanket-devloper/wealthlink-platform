package com.wealthlink.ledger.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Journal entity lifecycle callbacks (@PrePersist / @PreUpdate).
 * No Spring context or database required — exercises the POJO directly.
 */
class JournalTest {

    @Test
    void onCreate_defaultsStatusToDraft_whenNotSet() {
        Journal journal = Journal.builder()
                .journalReference("REF-001")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .build();

        journal.onCreate();

        assertThat(journal.getStatus()).isEqualTo(JournalStatus.DRAFT);
    }

    @Test
    void onCreate_defaultsJournalTypeToTrade_whenNotSet() {
        Journal journal = Journal.builder()
                .journalReference("REF-002")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .build();

        journal.onCreate();

        assertThat(journal.getJournalType()).isEqualTo(JournalType.TRADE);
    }

    @Test
    void onCreate_setsCreatedAtAndUpdatedAt_whenNotSet() {
        Journal journal = Journal.builder()
                .journalReference("REF-003")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .build();

        journal.onCreate();

        assertThat(journal.getCreatedAt()).isNotNull();
        assertThat(journal.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_doesNotOverrideExplicitStatus() {
        Journal journal = Journal.builder()
                .journalReference("REF-004")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .status(JournalStatus.POSTED)
                .build();

        journal.onCreate();

        assertThat(journal.getStatus()).isEqualTo(JournalStatus.POSTED);
    }

    @Test
    void onCreate_doesNotOverrideExplicitJournalType() {
        Journal journal = Journal.builder()
                .journalReference("REF-005")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .journalType(JournalType.FEE)
                .build();

        journal.onCreate();

        assertThat(journal.getJournalType()).isEqualTo(JournalType.FEE);
    }

    @Test
    void onUpdate_refreshesUpdatedAt_andPreservesCreatedAt() {
        Journal journal = Journal.builder()
                .journalReference("REF-006")
                .description("Test journal")
                .journalDate(java.time.LocalDate.now())
                .build();
        journal.onCreate();
        var createdAt = journal.getCreatedAt();

        journal.onUpdate();

        assertThat(journal.getCreatedAt()).isEqualTo(createdAt);
        assertThat(journal.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
