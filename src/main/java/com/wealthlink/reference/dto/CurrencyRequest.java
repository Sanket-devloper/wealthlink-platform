package com.wealthlink.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CurrencyRequest(
        @NotBlank @Size(min = 3, max = 3, message = "isoCode must be exactly 3 letters (e.g. NOK)") String isoCode,
        @NotBlank String name,
        @NotNull Short minorUnitDigits
) {}
