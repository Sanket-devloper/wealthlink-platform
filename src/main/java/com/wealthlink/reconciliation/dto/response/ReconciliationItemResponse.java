package com.wealthlink.reconciliation.dto.response;

import com.wealthlink.reconciliation.entity.ReconciliationMatchStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationItemResponse {
    private UUID id;
    private UUID reconciliationRunId;
    private UUID externalRecordId;
    private String externalReference;
    private String internalReferenceType;
    private UUID internalReferenceId;
    private ReconciliationMatchStatus matchStatus;
    private String differenceDetails;
    private Instant createdAt;
}