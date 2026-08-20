package com.wealthlink.marketdata.dto;

import com.wealthlink.marketdata.entity.RateType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FxRateResponse(

        UUID id,

        UUID baseCurrencyId,

        UUID quoteCurrencyId,

        LocalDate rateDate,

        RateType rateType,

        UUID sourceId,

        BigDecimal rate

) {
}