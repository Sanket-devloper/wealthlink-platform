package com.wealthlink.importdata;

import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.importdata.entity.ImportBatch;
import com.wealthlink.importdata.entity.ImportBatchStatus;
import com.wealthlink.importdata.entity.ImportItem;
import com.wealthlink.importdata.entity.ImportItemStatus;
import com.wealthlink.importdata.entity.ImportJob;
import com.wealthlink.importdata.entity.ImportJobStatus;
import com.wealthlink.importdata.entity.ImportJobType;
import com.wealthlink.importdata.repository.ImportBatchRepository;
import com.wealthlink.importdata.repository.ImportItemRepository;
import com.wealthlink.importdata.repository.ImportJobRepository;
import com.wealthlink.support.AbstractIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer integration tests for the import pipeline.
 *
 * Covers:
 * - IMPORT_JOB defaults
 * - IMPORT_BATCH idempotency
 * - IMPORT_ITEM persistence and failure isolation
 * - IMPORT_BATCH counters
 */
class ImportRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private ImportBatchRepository importBatchRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private ProviderRepository providerRepository;

    private ImportJob jobFixture() {

        Provider morningstar = providerRepository
                .findByCode("MORNINGSTAR")
                .orElseThrow();

        return importJobRepository.saveAndFlush(
                ImportJob.builder()
                        .name("Daily NAV Import")
                        .provider(morningstar)
                        .jobType(ImportJobType.FUND_PRICE_IMPORT)
                        .build()
        );
    }

    @Test
    void importJobDefaultsToActiveStatus() {

        ImportJob job = jobFixture();

        assertThat(job.getStatus())
                .isEqualTo(ImportJobStatus.ACTIVE);

        assertThat(job.getCreatedAt())
                .isNotNull();
    }

    @Test
    void importBatchIdempotencyKeyPreventsADuplicateCompletedBatch() {

        ImportJob job = jobFixture();

        ImportBatch first =
                importBatchRepository.saveAndFlush(
                        ImportBatch.builder()
                                .importJob(job)
                                .idempotencyKey(
                                        "2026-08-14-morningstar-nav"
                                )
                                .status(ImportBatchStatus.COMPLETED)
                                .build()
                );

        assertThat(first.getId())
                .isNotNull();

        // Retry with the same idempotency key must be rejected
        // at the database level.
        ImportBatch retry =
                ImportBatch.builder()
                        .importJob(job)
                        .idempotencyKey(
                                "2026-08-14-morningstar-nav"
                        )
                        .status(ImportBatchStatus.COMPLETED)
                        .build();

        assertThatThrownBy(
                () -> importBatchRepository.saveAndFlush(retry)
        )
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(
                importBatchRepository.findByIdempotencyKey(
                        "2026-08-14-morningstar-nav"
                )
        )
                .isPresent();
    }

    @Test
    void failedImportItemDoesNotRemoveSuccessfulItemsFromSameBatch() {

        ImportJob job = jobFixture();

        ImportBatch batch =
                importBatchRepository.saveAndFlush(
                        ImportBatch.builder()
                                .importJob(job)
                                .idempotencyKey(
                                        "2026-08-20-transaction-isolation"
                                )
                                .status(ImportBatchStatus.RUNNING)
                                .build()
                );

        ImportItem successItem1 =
                importItemRepository.saveAndFlush(
                        ImportItem.builder()
                                .importBatch(batch)
                                .rawPayload(
                                        "{\"row\":\"GOOD-1\"}"
                                )
                                .status(ImportItemStatus.SUCCESS)
                                .build()
                );

        ImportItem failedItem =
                importItemRepository.saveAndFlush(
                        ImportItem.builder()
                                .importBatch(batch)
                                .rawPayload(
                                        "{\"row\":\"BAD-1\"}"
                                )
                                .status(ImportItemStatus.FAILED)
                                .errorDetails(
                                        "Invalid fund price"
                                )
                                .build()
                );

        ImportItem successItem2 =
                importItemRepository.saveAndFlush(
                        ImportItem.builder()
                                .importBatch(batch)
                                .rawPayload(
                                        "{\"row\":\"GOOD-2\"}"
                                )
                                .status(ImportItemStatus.SUCCESS)
                                .build()
                );

        assertThat(
                importItemRepository.findByImportBatchId(
                        batch.getId()
                )
        )
                .hasSize(3);

        assertThat(
                importItemRepository
                        .findByImportBatchIdAndStatus(
                                batch.getId(),
                                ImportItemStatus.SUCCESS
                        )
        )
                .hasSize(2);

        assertThat(
                importItemRepository
                        .findByImportBatchIdAndStatus(
                                batch.getId(),
                                ImportItemStatus.FAILED
                        )
        )
                .hasSize(1);

        assertThat(failedItem.getErrorDetails())
                .isEqualTo("Invalid fund price");
    }

    @Test
    void importItemFailurePersistsErrorDetailsWithoutBlockingTheBatch() {

        ImportJob job = jobFixture();

        ImportBatch batch =
                importBatchRepository.saveAndFlush(
                        ImportBatch.builder()
                                .importJob(job)
                                .idempotencyKey(
                                        "2026-08-15-morningstar-nav"
                                )
                                .build()
                );

        ImportItem failedItem =
                importItemRepository.saveAndFlush(
                        ImportItem.builder()
                                .importBatch(batch)
                                .rawPayload(
                                        "{\"externalFundId\":\"BAD-ROW\"}"
                                )
                                .status(ImportItemStatus.FAILED)
                                .errorDetails(
                                        "Unknown fund_share_class mapping for external id BAD-ROW"
                                )
                                .build()
                );

        ImportItem succeededItem =
                importItemRepository.saveAndFlush(
                        ImportItem.builder()
                                .importBatch(batch)
                                .rawPayload(
                                        "{\"externalFundId\":\"GOOD-ROW\"}"
                                )
                                .status(ImportItemStatus.SUCCESS)
                                .build()
                );

        assertThat(failedItem.getErrorDetails())
                .isNotBlank();

        assertThat(succeededItem.getErrorDetails())
                .isNull();

        assertThat(
                importItemRepository.findByImportBatchId(
                        batch.getId()
                )
        )
                .hasSize(2);

        assertThat(
                importItemRepository.findByImportBatchIdAndStatus(
                        batch.getId(),
                        ImportItemStatus.FAILED
                )
        )
                .hasSize(1);
    }

    /**
     * Day 3 integration test:
     *
     * Verifies that IMPORT_BATCH stores and retrieves
     * total, successful and failed item counters correctly.
     */
    @Test
    void importBatchCountersTrackSuccessfulAndFailedItems() {

        ImportJob job = jobFixture();

        ImportBatch batch =
                importBatchRepository.saveAndFlush(
                        ImportBatch.builder()
                                .importJob(job)
                                .idempotencyKey(
                                        "2026-08-20-counter-test"
                                )
                                .status(ImportBatchStatus.RUNNING)
                                .totalItems(3)
                                .successCount(2)
                                .failureCount(1)
                                .build()
                );

        assertThat(batch.getTotalItems())
                .isEqualTo(3);

        assertThat(batch.getSuccessCount())
                .isEqualTo(2);

        assertThat(batch.getFailureCount())
                .isEqualTo(1);
    }
}