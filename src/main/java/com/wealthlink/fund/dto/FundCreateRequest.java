package com.wealthlink.fund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FundCreateRequest(

        @NotBlank(message = "ISIN is required")
        @Size(min = 12, max = 12, message = "ISIN must be exactly 12 characters")
        @Pattern(
                regexp = "^[A-Z]{2}[A-Z0-9]{9}[0-9]$",
                message = "ISIN must be a valid 12-character format"
        )
        String isin,

        @NotBlank(message = "Fund name is required")
        @Size(max = 255, message = "Fund name must not exceed 255 characters")
        String name,

        @NotNull(message = "Base currency ID is required")
        UUID baseCurrencyId,

        @NotNull(message = "Domicile country ID is required")
        UUID domicileCountryId,

        LocalDate inceptionDate
) {
}