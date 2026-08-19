package com.wealthlink.dividend.dto.response;

import com.wealthlink.dividend.entity.DividendAllocationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendAllocationResponse {
    private UUID id;
    private UUID dividendEventId;
    private UUID portfolioId;
    private BigDecimal holdingQuantity;
    private BigDecimal grossAmount;
    private BigDecimal taxWithheldAmount;
    private BigDecimal netAmount;
    private String currency;
    private DividendAllocationStatus status;
    private UUID journalId;
    private Instant processedAt;
}