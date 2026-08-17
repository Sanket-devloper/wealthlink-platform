package com.wealthlink.importdata;

import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.importdata.entity.*;
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
 * Repository-layer test (DEV2-D2 / DEV2-D3) for the import pipeline.
 * {@link #importBatchIdempotencyKeyPreventsADuplicateCompletedBatch()} is
 * Required Cross-Module Test 3 from the architecture doc: run an
 * IMPORT_BATCH with key X, retry with key X -> no duplicate completed
 * batch.
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
        Provider morningstar = providerRepository.findByCode("MORNINGSTAR").orElseThrow();
        return importJobRepository.saveAndFlush(ImportJob.builder()
                .name("Daily NAV Import")
                .provider(morningstar)
                .jobType(ImportJobType.FUND_PRICE_IMPORT)
                .build());
    }

    @Test
    void importJobDefaultsToActiveStatus() {
        ImportJob job = jobFixture();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.ACTIVE);
        assertThat(job.getCreatedAt()).isNotNull();
    }

    @Test
    void importBatchIdempotencyKeyPreventsADuplicateCompletedBatch() {
        ImportJob job = jobFixture();

        ImportBatch first = importBatchRepository.saveAndFlush(ImportBatch.builder()
                .importJob(job)
                .idempotencyKey("2026-08-14-morningstar-nav")
                .status(ImportBatchStatus.COMPLETED)
                .build());

        assertThat(first.getId()).isNotNull();

        // Retry with the same idempotency key must be rejected at the DB
        // level, matching Required Cross-Module Test 3.
        ImportBatch retry = ImportBatch.builder()
                .importJob(job)
                .idempotencyKey("2026-08-14-morningstar-nav")
                .status(ImportBatchStatus.COMPLETED)
                .build();

        assertThatThrownBy(() -> importBatchRepository.saveAndFlush(retry))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(importBatchRepository.findByIdempotencyKey("2026-08-14-morningstar-nav")).isPresent();
    }

    @Test
    void importItemFailurePersistsErrorDetailsWithoutBlockingTheBatch() {
        ImportJob job = jobFixture();
        ImportBatch batch = importBatchRepository.saveAndFlush(ImportBatch.builder()
                .importJob(job)
                .idempotencyKey("2026-08-15-morningstar-nav")
                .build());

        ImportItem failedItem = importItemRepository.saveAndFlush(ImportItem.builder()
                .importBatch(batch)
                .rawPayload("{\"externalFundId\":\"BAD-ROW\"}")
                .status(ImportItemStatus.FAILED)
                .errorDetails("Unknown fund_share_class mapping for external id BAD-ROW")
                .build());

        ImportItem succeededItem = importItemRepository.saveAndFlush(ImportItem.builder()
                .importBatch(batch)
                .rawPayload("{\"externalFundId\":\"GOOD-ROW\"}")
                .status(ImportItemStatus.SUCCESS)
                .build());

        assertThat(failedItem.getErrorDetails()).isNotBlank();
        assertThat(succeededItem.getErrorDetails()).isNull();
        assertThat(importItemRepository.findByImportBatchId(batch.getId())).hasSize(2);
        assertThat(importItemRepository.findByImportBatchIdAndStatus(batch.getId(), ImportItemStatus.FAILED))
                .hasSize(1);
    }
}
