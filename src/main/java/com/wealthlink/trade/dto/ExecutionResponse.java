package com.wealthlink.trade.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ExecutionResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal executedQuantity;
    private BigDecimal executedPrice;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private String executionStatus;
    private UUID journalId;
}
