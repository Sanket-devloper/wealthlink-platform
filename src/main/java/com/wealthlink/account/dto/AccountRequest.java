package com.wealthlink.account.dto;

import com.wealthlink.account.entity.AccountStatus;
import com.wealthlink.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountRequest(
        @NotBlank String accountNumber,
        @NotNull AccountType accountType,
        @NotNull UUID currencyId,
        @NotNull UUID countryId,
        AccountStatus status
) {}
