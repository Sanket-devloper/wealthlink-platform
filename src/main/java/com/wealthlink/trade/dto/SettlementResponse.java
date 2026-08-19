package com.wealthlink.trade.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SettlementResponse {
    private UUID id;
    private UUID tradeExecutionId;
    private LocalDate settlementDate;
    private BigDecimal settlementAmount;
    private String currency;
    private String settlementStatus;
}
