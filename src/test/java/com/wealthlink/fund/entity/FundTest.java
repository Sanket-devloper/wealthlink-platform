package com.wealthlink.fund.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-level unit test (Day 1 / DEV2-D1 deliverable) - no database,
 * verifies the @PrePersist defaulting logic directly on the entity.
 */
class FundTest {

    @Test
    void onCreateDefaultsStatusToActiveAndSetsTimestampsWhenNotSet() {
        Fund fund = Fund.builder()
                .isin("IE00TEST0001")
                .name("Test Global Equity Fund")
                .build();

        fund.onCreate();

        assertThat(fund.getStatus()).isEqualTo(FundStatus.ACTIVE);
        assertThat(fund.getCreatedAt()).isNotNull();
        assertThat(fund.getUpdatedAt()).isNotNull();
        assertThat(fund.getCreatedAt()).isEqualTo(fund.getUpdatedAt());
    }

    @Test
    void onCreateDoesNotOverrideAnExplicitStatus() {
        Fund fund = Fund.builder()
                .isin("IE00TEST0002")
                .name("Test Bond Fund")
                .status(FundStatus.SUSPENDED)
                .build();

        fund.onCreate();

        assertThat(fund.getStatus()).isEqualTo(FundStatus.SUSPENDED);
    }

    @Test
    void onUpdateRefreshesUpdatedAtOnly() {
        Fund fund = Fund.builder()
                .isin("IE00TEST0003")
                .name("Test Multi-Asset Fund")
                .build();
        fund.onCreate();
        var createdAt = fund.getCreatedAt();

        fund.onUpdate();

        assertThat(fund.getCreatedAt()).isEqualTo(createdAt);
        assertThat(fund.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
