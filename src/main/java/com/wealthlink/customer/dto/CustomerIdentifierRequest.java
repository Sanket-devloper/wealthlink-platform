package com.wealthlink.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomerIdentifierRequest(
        @NotBlank String idType,
        @NotBlank String idValue,
        @NotNull UUID issuingCountryId
) {}
