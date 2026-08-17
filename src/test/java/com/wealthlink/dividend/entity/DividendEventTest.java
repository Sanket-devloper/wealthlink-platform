package com.wealthlink.dividend.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DividendEventTest {

    @Test
    void onCreateSetsDefaultStatusAndTimestamp() {
        DividendEvent event = DividendEvent.builder()
                .exDate(LocalDate.now())
                .recordDate(LocalDate.now())
                .paymentDate(LocalDate.now().plusDays(2))
                .dividendPerUnit(new BigDecimal("1.50000000"))
                .source("MANUAL")
                .build();

        event.onCreate();

        assertThat(event.getStatus()).isEqualTo(DividendEventStatus.DECLARED);
        assertThat(event.getCreatedAt()).isNotNull();
    }
}