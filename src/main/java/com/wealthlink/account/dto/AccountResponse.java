package com.wealthlink.account.dto;

import com.wealthlink.account.entity.AccountStatus;
import com.wealthlink.account.entity.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType accountType,
        UUID currencyId,
        String currencyIsoCode,
        UUID countryId,
        String countryIsoCode,
        AccountStatus status,
        Instant openedAt
) {}
