package com.wealthlink.importdata.dto;

import com.wealthlink.importdata.entity.ImportItemStatus;

import java.time.Instant;
import java.util.UUID;

public record ImportItemResponse(

        UUID id,

        UUID importBatchId,

        String rawPayload,

        ImportItemStatus status,

        String errorDetails,

        UUID fundPriceId,

        Instant processedAt

) {
}