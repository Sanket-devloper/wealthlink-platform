package com.wealthlink.reference.dto;

import com.wealthlink.reference.entity.MarketStatus;

import java.util.UUID;

public record MarketResponse(
        UUID id,
        UUID countryId,
        String countryIsoCode,
        String name,
        String micCode,
        String timezone,
        MarketStatus status
) {}
