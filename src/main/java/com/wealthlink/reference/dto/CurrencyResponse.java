package com.wealthlink.reference.dto;

import java.util.UUID;

public record CurrencyResponse(
        UUID id,
        String isoCode,
        String name,
        Short minorUnitDigits
) {}
