package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ShareClassStatus;

import java.util.UUID;

public record FundShareClassResponse(

        UUID id,

        UUID fundId,

        String classCode,

        String name,

        UUID currencyId,

        ShareClassStatus status
) {
}