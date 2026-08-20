package com.wealthlink.marketdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FxRateSourceCreateRequest(

        @NotBlank(message = "Code is required")
        @Size(max = 30, message = "Code must not exceed 30 characters")
        String code,

        @NotBlank(message = "Name is required")
        String name
) {
}