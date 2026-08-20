package com.wealthlink.portfolio.service;

import com.wealthlink.portfolio.dto.CreatePortfolioRequest;
import com.wealthlink.portfolio.dto.PortfolioResponse;
import com.wealthlink.portfolio.dto.ValuationResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioService {

    List<PortfolioResponse> getAll();

    PortfolioResponse getById(UUID id);

    PortfolioResponse createPortfolio(CreatePortfolioRequest request);

    ValuationResponse getValuation(UUID portfolioId, LocalDate valuationDate);
}
