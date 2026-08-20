package com.wealthlink.fund.dto;

import com.wealthlink.fund.entity.ProviderStatus;

import java.util.UUID;

public record ProviderResponse(

        UUID id,

        String code,

        String name,

        ProviderStatus status
) {
}
