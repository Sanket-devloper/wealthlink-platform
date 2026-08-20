package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ProviderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProviderCreateRequest(

        @NotBlank(message = "Provider code is required")
        @Size(max = 30, message = "Provider code must not exceed 30 characters")
        String code,

        @NotBlank(message = "Provider name is required")
        String name,

        ProviderStatus status
) {
}