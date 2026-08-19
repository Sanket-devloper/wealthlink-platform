package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ShareClassStatus;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FundShareClassUpdateRequest(

        UUID fundId,

        @Size(
                max = 30,
                message = "Class code must not exceed 30 characters"
        )
        String classCode,

        @Size(
                max = 255,
                message = "Share class name must not exceed 255 characters"
        )
        String name,

        UUID currencyId,

        ShareClassStatus status
) {
}