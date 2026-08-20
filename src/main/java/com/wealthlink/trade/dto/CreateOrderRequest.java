package com.wealthlink.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private UUID portfolioId;
    private UUID fundShareClassId;
    private String orderType;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private UUID currencyId;
    private String idempotencyKey;
}
