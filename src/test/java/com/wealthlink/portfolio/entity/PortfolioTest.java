package com.wealthlink.portfolio.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioTest {

    @Test
    void prePersistSetsDefaults() {
        Portfolio portfolio = new Portfolio();
        assertThat(portfolio.getOpenedAt()).isNull();
        assertThat(portfolio.getStatus()).isNull();

        portfolio.onCreate();

        assertThat(portfolio.getOpenedAt()).isNotNull();
        assertThat(portfolio.getStatus()).isEqualTo(PortfolioStatus.ACTIVE);
    }
    
    @Test
    void prePersistKeepsExistingValues() {
        Instant past = Instant.parse("2020-01-01T00:00:00Z");
        Portfolio portfolio = Portfolio.builder()
                .openedAt(past)
                .status(PortfolioStatus.CLOSED)
                .build();

        portfolio.onCreate();

        assertThat(portfolio.getOpenedAt()).isEqualTo(past);
        assertThat(portfolio.getStatus()).isEqualTo(PortfolioStatus.CLOSED);
    }
}
