package com.wealthlink.trade.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TradeOrder entity lifecycle callbacks (@PrePersist / @PreUpdate).
 * No Spring context or database required.
 */
class TradeOrderTest {

    @Test
    void onCreate_defaultsStatusToPending_whenNotSet() {
        TradeOrder order = new TradeOrder();

        order.onCreate();

        assertThat(order.getStatus()).isEqualTo(TradeOrderStatus.PENDING);
    }

    @Test
    void onCreate_setsTimestamps_whenNotSet() {
        TradeOrder order = new TradeOrder();

        order.onCreate();

        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_doesNotOverrideExplicitStatus() {
        TradeOrder order = TradeOrder.builder()
                .status(TradeOrderStatus.CANCELLED)
                .build();

        order.onCreate();

        assertThat(order.getStatus()).isEqualTo(TradeOrderStatus.CANCELLED);
    }

    @Test
    void onUpdate_refreshesUpdatedAt_andPreservesCreatedAt() {
        TradeOrder order = new TradeOrder();
        order.onCreate();
        var createdAt = order.getCreatedAt();

        order.onUpdate();

        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
