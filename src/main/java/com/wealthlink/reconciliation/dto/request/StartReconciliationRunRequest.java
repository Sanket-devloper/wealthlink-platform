package com.wealthlink.reconciliation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartReconciliationRunRequest {

    @NotBlank(message = "Run type is required (e.g., HOLDINGS, CASH, TRANSACTIONS)")
    private String runType;

    @NotNull(message = "Business date is required")
    private LocalDate businessDate;

    private UUID initiatedById;
}