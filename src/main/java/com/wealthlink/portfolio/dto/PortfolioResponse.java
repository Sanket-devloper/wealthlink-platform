package com.wealthlink.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioResponse {
    private UUID id;
    private String portfolioNumber;
    private String portfolioType;
    private String baseCurrency;
    private String status;
    private String accountNumber;
}
