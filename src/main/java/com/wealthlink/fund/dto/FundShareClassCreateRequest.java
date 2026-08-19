package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ShareClassStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FundShareClassCreateRequest(

        @NotNull(message = "Fund ID is required")
        UUID fundId,

        @NotBlank(message = "Class code is required")
        @Size(
                max = 30,
                message = "Class code must not exceed 30 characters"
        )
        String classCode,

        @NotBlank(message = "Share class name is required")
        @Size(
                max = 255,
                message = "Share class name must not exceed 255 characters"
        )
        String name,

        @NotNull(message = "Currency ID is required")
        UUID currencyId,

        ShareClassStatus status
) {
}