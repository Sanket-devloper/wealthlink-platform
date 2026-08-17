package com.wealthlink.portfolio.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioValuationSnapshotTest {

    @Test
    void prePersistSetsDefaults() {
        PortfolioValuationSnapshot snapshot = new PortfolioValuationSnapshot();
        assertThat(snapshot.getCreatedAt()).isNull();

        snapshot.onCreate();

        assertThat(snapshot.getCreatedAt()).isNotNull();
    }
    
    @Test
    void prePersistKeepsExistingValues() {
        Instant past = Instant.parse("2020-01-01T00:00:00Z");
        PortfolioValuationSnapshot snapshot = PortfolioValuationSnapshot.builder()
                .createdAt(past)
                .build();

        snapshot.onCreate();

        assertThat(snapshot.getCreatedAt()).isEqualTo(past);
    }
}
