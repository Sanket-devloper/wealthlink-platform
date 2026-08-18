package com.wealthlink.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CountryRequest(
        @NotBlank @Size(min = 2, max = 2, message = "isoCode must be exactly 2 letters (e.g. NO)") String isoCode,
        @NotBlank String name,
        @NotNull UUID defaultCurrencyId,
        @NotBlank String timezone
) {}
