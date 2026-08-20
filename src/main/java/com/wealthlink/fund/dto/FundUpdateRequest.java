package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.FundStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FundUpdateRequest(

        @Size(
                max = 255,
                message = "Fund name must not exceed 255 characters"
        )
        String name,

        UUID baseCurrencyId,

        UUID domicileCountryId,

        FundStatus status,

        LocalDate inceptionDate
) {
}