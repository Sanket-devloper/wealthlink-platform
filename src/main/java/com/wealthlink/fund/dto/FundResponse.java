package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.FundStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FundResponse(

        UUID id,

        String isin,

        String name,

        UUID baseCurrencyId,

        UUID domicileCountryId,

        FundStatus status,

        LocalDate inceptionDate,

        Instant createdAt,

        Instant updatedAt
) {
}