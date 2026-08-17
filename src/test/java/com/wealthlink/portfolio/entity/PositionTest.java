package com.wealthlink.portfolio.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @Test
    void prePersistSetsDefaults() {
        Position position = new Position();
        assertThat(position.getComputedAt()).isNull();
        assertThat(position.getStatus()).isNull();

        position.onCreate();

        assertThat(position.getComputedAt()).isNotNull();
        assertThat(position.getStatus()).isEqualTo(PositionStatus.OPEN);
    }
    
    @Test
    void prePersistKeepsExistingValues() {
        Instant past = Instant.parse("2020-01-01T00:00:00Z");
        Position position = Position.builder()
                .computedAt(past)
                .status(PositionStatus.CLOSED)
                .build();

        position.onCreate();

        assertThat(position.getComputedAt()).isEqualTo(past);
        assertThat(position.getStatus()).isEqualTo(PositionStatus.CLOSED);
    }
}
