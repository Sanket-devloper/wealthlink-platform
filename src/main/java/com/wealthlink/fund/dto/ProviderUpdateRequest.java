package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ProviderStatus;
import jakarta.validation.constraints.Size;

public record ProviderUpdateRequest(

        @Size(max = 30, message = "Provider code must not exceed 30 characters")
        String code,

        String name,

        ProviderStatus status
) {
}