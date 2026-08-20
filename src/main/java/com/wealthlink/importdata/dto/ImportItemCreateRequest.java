package com.wealthlink.importdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ImportItemCreateRequest(

        @NotNull(message = "Import batch ID is required")
        UUID importBatchId,

        @NotBlank(message = "Raw payload is required")
        String rawPayload

) {
}