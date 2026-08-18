package com.wealthlink.reference.dto;

import java.util.UUID;

public record CountryResponse(
        UUID id,
        String isoCode,
        String name,
        UUID defaultCurrencyId,
        String defaultCurrencyIsoCode,
        String timezone
) {}
