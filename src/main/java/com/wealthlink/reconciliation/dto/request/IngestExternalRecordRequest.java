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
public class IngestExternalRecordRequest {

    @NotNull(message = "Reconciliation Run ID is required")
    private UUID reconciliationRunId;

    @NotNull(message = "Provider ID is required")
    private UUID providerId;

    @NotBlank(message = "External reference is required")
    private String externalReference;

    @NotBlank(message = "Raw payload JSON is required")
    private String rawPayload;
}