package com.wealthlink.importdata.dto;

import com.wealthlink.importdata.entity.ImportJobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ImportJobCreateRequest(

        @NotBlank(message = "Import job name is required")
        String name,

        @NotNull(message = "Provider ID is required")
        UUID providerId,

        @NotNull(message = "Job type is required")
        ImportJobType jobType

) {
}