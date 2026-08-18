package com.wealthlink.account.dto;

import com.wealthlink.account.entity.OwnershipRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountOwnerRequest(
        @NotNull UUID customerId,
        @NotNull OwnershipRole ownershipRole
) {}
