package com.wealthlink.customer.dto;

import java.util.UUID;

public record CustomerIdentifierResponse(
        UUID id,
        UUID customerId,
        String idType,
        String idValue,
        UUID issuingCountryId,
        String issuingCountryIsoCode
) {}
