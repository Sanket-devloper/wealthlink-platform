package com.wealthlink.customer.dto;

import com.wealthlink.customer.entity.ContactType;

import java.util.UUID;

public record CustomerContactResponse(
        UUID id,
        UUID customerId,
        ContactType contactType,
        String value,
        boolean isPrimary
) {}
