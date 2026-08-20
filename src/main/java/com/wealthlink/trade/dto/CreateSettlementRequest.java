package com.wealthlink.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSettlementRequest {
    private UUID tradeExecutionId;
    private LocalDate settlementDate;
    private BigDecimal settlementAmount;
    private UUID currencyId;
}
