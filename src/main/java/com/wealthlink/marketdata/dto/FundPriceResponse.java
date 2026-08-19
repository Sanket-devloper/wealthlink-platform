package com.wealthlink.marketdata.dto;

import com.wealthlink.marketdata.entity.PriceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FundPriceResponse(

        UUID id,

        UUID fundShareClassId,

        LocalDate priceDate,

        PriceType priceType,

        UUID providerId,

        UUID currencyId,

        BigDecimal price,

        UUID importBatchId

) {
}