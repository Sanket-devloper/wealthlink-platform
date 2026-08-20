package com.wealthlink.reconciliation.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationRunTest {

    @Test
    void onCreateSetsDefaults() {
        ReconciliationRun run = ReconciliationRun.builder()
                .runType("HOLDINGS_RECONCILIATION")
                .businessDate(LocalDate.now())
                .build();

        run.onCreate();

        assertThat(run.getStatus()).isEqualTo(ReconciliationRunStatus.PENDING);
        assertThat(run.getStartedAt()).isNotNull();
    }
}