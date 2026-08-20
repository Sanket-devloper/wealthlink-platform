package com.wealthlink.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecordExecutionRequest {
    private UUID orderId;
    private LocalDate tradeDate;
    private BigDecimal executedQuantity;
    private BigDecimal executedPrice;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private UUID currencyId;
    private String externalReference;
}
