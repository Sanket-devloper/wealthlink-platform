package com.wealthlink.importdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ImportBatchCreateRequest(

        @NotNull(message = "Import job ID is required")
        UUID importJobId,

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey

) {
}