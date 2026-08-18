package com.wealthlink.account.dto;

import com.wealthlink.account.entity.OwnershipRole;

import java.util.UUID;

public record AccountOwnerResponse(
        UUID id,
        UUID accountId,
        UUID customerId,
        String customerNumber,
        OwnershipRole ownershipRole
) {}
