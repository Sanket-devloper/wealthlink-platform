package com.wealthlink.importdata.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-level unit test (Day 1 / DEV2-D1 deliverable) - no database,
 * verifies the @PrePersist defaulting logic directly on the entity.
 */
class ImportBatchTest {

    @Test
    void onCreateDefaultsStatusToPendingAndSetsStartedAtWhenNotSet() {
        ImportBatch batch = ImportBatch.builder()
                .idempotencyKey("batch-key-1")
                .build();

        batch.onCreate();

        assertThat(batch.getStatus()).isEqualTo(ImportBatchStatus.PENDING);
        assertThat(batch.getStartedAt()).isNotNull();
    }

    @Test
    void onCreateDoesNotOverrideAnExplicitStatusOrStartedAt() {
        var explicitStartedAt = java.time.Instant.parse("2026-08-14T09:00:00Z");

        ImportBatch batch = ImportBatch.builder()
                .idempotencyKey("batch-key-2")
                .status(ImportBatchStatus.RUNNING)
                .startedAt(explicitStartedAt)
                .build();

        batch.onCreate();

        assertThat(batch.getStatus()).isEqualTo(ImportBatchStatus.RUNNING);
        assertThat(batch.getStartedAt()).isEqualTo(explicitStartedAt);
    }

    @Test
    void countersDefaultToZero() {
        ImportBatch batch = ImportBatch.builder()
                .idempotencyKey("batch-key-3")
                .build();

        assertThat(batch.getTotalItems()).isZero();
        assertThat(batch.getSuccessCount()).isZero();
        assertThat(batch.getFailureCount()).isZero();
    }
}
