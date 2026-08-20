package com.wealthlink.fund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FundProviderMappingCreateRequest(

        @NotNull(message = "Fund share class ID is required")
        UUID fundShareClassId,

        @NotNull(message = "Provider ID is required")
        UUID providerId,

        @NotBlank(message = "External fund ID is required")
        String externalFundId
) {
}