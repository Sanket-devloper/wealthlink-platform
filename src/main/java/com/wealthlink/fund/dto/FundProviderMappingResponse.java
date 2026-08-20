package com.wealthlink.fund.dto;

import java.util.UUID;

public record FundProviderMappingResponse(

        UUID id,

        UUID fundShareClassId,

        UUID providerId,

        String externalFundId
) {
}