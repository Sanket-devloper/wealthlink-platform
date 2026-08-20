package com.wealthlink.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PositionResponse {
    private UUID id;
    private UUID fundShareClassId;
    private LocalDate positionDate;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal marketValue;
    private String currency;
    
    // Additional fields for Valuation context
    private String fundName;
    private BigDecimal price;
}
