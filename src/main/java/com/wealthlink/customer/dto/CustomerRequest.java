package com.wealthlink.customer.dto;

import com.wealthlink.customer.entity.CustomerStatus;
import com.wealthlink.customer.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomerRequest(
        @NotBlank String customerNumber,
        @NotNull CustomerType customerType,
        CustomerStatus status,
        @NotNull UUID countryId,
        @NotNull UUID taxResidencyCountryId
) {}
