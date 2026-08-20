package com.wealthlink.reconciliation.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationResolutionResponse {
    private UUID id;
    private UUID reconciliationItemId;
    private UUID resolvedById;
    private String resolvedByUsername;
    private String resolutionType;
    private String resolutionNotes;
    private Instant resolvedAt;
}