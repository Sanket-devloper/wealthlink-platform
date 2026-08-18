package com.wealthlink.customer.dto;

import com.wealthlink.customer.entity.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerContactRequest(
        @NotNull ContactType contactType,
        @NotBlank String value,
        boolean isPrimary
) {}
