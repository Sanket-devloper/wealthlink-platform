package com.wealthlink.importdata.dto;

import com.wealthlink.importdata.entity.ImportBatchStatus;

import java.time.Instant;
import java.util.UUID;

public record ImportBatchResponse(

        UUID id,

        UUID importJobId,

        String idempotencyKey,

        ImportBatchStatus status,

        Instant startedAt,

        Instant completedAt,

        Integer totalItems,

        Integer successCount,

        Integer failureCount,

        Integer version

) {
}