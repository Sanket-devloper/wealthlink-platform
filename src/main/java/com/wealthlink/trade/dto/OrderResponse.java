package com.wealthlink.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private UUID id;
    private UUID portfolioId;
    private UUID fundShareClassId;
    private String orderType;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private String currency;
    private String status;
    private String idempotencyKey;
    private Instant createdAt;
    
    // For GET /orders/{id}
    private BigDecimal executedQuantity;
    private BigDecimal executedAmount;
}
