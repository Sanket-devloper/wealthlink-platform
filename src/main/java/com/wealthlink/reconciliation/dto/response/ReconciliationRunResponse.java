package com.wealthlink.reconciliation.dto.response;

import com.wealthlink.reconciliation.entity.ReconciliationRunStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationRunResponse {
    private UUID id;
    private String runType;
    private LocalDate businessDate;
    private Instant startedAt;
    private Instant completedAt;
    private ReconciliationRunStatus status;
    private UUID initiatedById;
    private String initiatedByUsername;
}