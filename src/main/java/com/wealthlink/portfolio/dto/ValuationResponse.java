package com.wealthlink.portfolio.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ValuationResponse {
    private UUID portfolioId;
    private LocalDate valuationDate;
    private BigDecimal totalValue;
    private String currency;
    private List<PositionResponse> positions;
}
