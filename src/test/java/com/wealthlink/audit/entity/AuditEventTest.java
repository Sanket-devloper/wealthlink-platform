package com.wealthlink.audit.entity;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventTest {

    @Test
    void onCreateSetsOccurredAtTimestamp() {
        AuditEvent audit = AuditEvent.builder()
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(UUID.randomUUID())
                .build();

        audit.onCreate();

        assertThat(audit.getOccurredAt()).isNotNull();
    }
}
