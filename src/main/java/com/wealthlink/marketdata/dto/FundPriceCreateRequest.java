package com.wealthlink.marketdata.dto;

import com.wealthlink.marketdata.entity.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FundPriceCreateRequest(

        @NotNull(message = "Fund share class ID is required")
        UUID fundShareClassId,

        @NotNull(message = "Price date is required")
        LocalDate priceDate,

        @NotNull(message = "Price type is required")
        PriceType priceType,

        @NotNull(message = "Provider ID is required")
        UUID providerId,

        @NotNull(message = "Currency ID is required")
        UUID currencyId,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        UUID importBatchId

) {
}