package com.wealthlink.reconciliation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveReconciliationItemRequest {

    @NotNull(message = "Resolver user ID is required")
    private UUID resolvedById;

    @NotBlank(message = "Resolution type is required (e.g., FORCE_MATCH, MANUAL_ADJUSTMENT, DISMISSED)")
    private String resolutionType;

    private String resolutionNotes;
}