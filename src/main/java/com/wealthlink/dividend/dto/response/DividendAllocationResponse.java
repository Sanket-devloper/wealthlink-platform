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
    private BigDecimal positionQuantity;
    private BigDecimal grossAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private String currencyCode;
    private DividendAllocationStatus status;
    private UUID journalId;
    private Instant createdAt;
}