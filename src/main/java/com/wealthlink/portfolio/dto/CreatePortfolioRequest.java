package com.wealthlink.portfolio.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreatePortfolioRequest {
    private UUID accountId;
    private String portfolioNumber;
    private String portfolioType;
    private UUID baseCurrencyId;
}
