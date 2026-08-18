package com.wealthlink.reference.dto;

import com.wealthlink.reference.entity.MarketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MarketRequest(
        @NotNull UUID countryId,
        @NotBlank String name,
        @NotBlank String micCode,
        @NotBlank String timezone,
        MarketStatus status
) {}
