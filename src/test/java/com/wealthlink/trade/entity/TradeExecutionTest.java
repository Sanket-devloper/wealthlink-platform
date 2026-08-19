package com.wealthlink.trade.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TradeExecution entity lifecycle callbacks (@PrePersist / @PreUpdate).
 * No Spring context or database required.
 */
class TradeExecutionTest {

    @Test
    void onCreate_defaultsStatusToPending_whenNotSet() {
        TradeExecution execution = new TradeExecution();

        execution.onCreate();

        assertThat(execution.getStatus()).isEqualTo(TradeExecutionStatus.PENDING);
    }

    @Test
    void onCreate_defaultsFeeAndTaxToZero_whenNotSet() {
        TradeExecution execution = new TradeExecution();

        execution.onCreate();

        assertThat(execution.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(execution.getTax()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void onCreate_setsTimestamps_whenNotSet() {
        TradeExecution execution = new TradeExecution();

        execution.onCreate();

        assertThat(execution.getCreatedAt()).isNotNull();
        assertThat(execution.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_doesNotOverrideExplicitFeeAndTax() {
        BigDecimal fee = BigDecimal.valueOf(5.50);
        BigDecimal tax = BigDecimal.valueOf(1.20);
        TradeExecution execution = TradeExecution.builder()
                .fee(fee)
                .tax(tax)
                .build();

        execution.onCreate();

        assertThat(execution.getFee()).isEqualByComparingTo(fee);
        assertThat(execution.getTax()).isEqualByComparingTo(tax);
    }

    @Test
    void onCreate_doesNotOverrideExplicitStatus() {
        TradeExecution execution = TradeExecution.builder()
                .status(TradeExecutionStatus.CONFIRMED)
                .build();

        execution.onCreate();

        assertThat(execution.getStatus()).isEqualTo(TradeExecutionStatus.CONFIRMED);
    }

    @Test
    void onUpdate_refreshesUpdatedAt_andPreservesCreatedAt() {
        TradeExecution execution = new TradeExecution();
        execution.onCreate();
        Instant createdAt = execution.getCreatedAt();

        execution.onUpdate();

        assertThat(execution.getCreatedAt()).isEqualTo(createdAt);
        assertThat(execution.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
