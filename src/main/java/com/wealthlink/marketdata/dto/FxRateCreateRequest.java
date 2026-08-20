package com.wealthlink.marketdata.dto;

import com.wealthlink.marketdata.entity.RateType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FxRateCreateRequest(

        @NotNull(message = "Base currency ID is required")
        UUID baseCurrencyId,

        @NotNull(message = "Quote currency ID is required")
        UUID quoteCurrencyId,

        @NotNull(message = "Rate date is required")
        LocalDate rateDate,

        @NotNull(message = "Rate type is required")
        RateType rateType,

        @NotNull(message = "Source ID is required")
        UUID sourceId,

        @NotNull(message = "Rate is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
        BigDecimal rate

) {
}