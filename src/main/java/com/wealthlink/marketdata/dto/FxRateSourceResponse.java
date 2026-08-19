package com.wealthlink.marketdata.dto;

import java.util.UUID;

public record FxRateSourceResponse(

        UUID id,

        String code,

        String name
) {
}