package com.wealthlink.customer.dto;

import com.wealthlink.customer.entity.CustomerStatus;
import com.wealthlink.customer.entity.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerNumber,
        CustomerType customerType,
        CustomerStatus status,
        UUID countryId,
        String countryIsoCode,
        UUID taxResidencyCountryId,
        String taxResidencyCountryIsoCode,
        Instant createdAt,
        Instant updatedAt
) {}
