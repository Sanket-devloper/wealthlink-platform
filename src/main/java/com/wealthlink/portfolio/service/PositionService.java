package com.wealthlink.portfolio.service;

import com.wealthlink.portfolio.dto.PositionResponse;
import com.wealthlink.trade.entity.TradeExecution;
import java.util.List;
import java.util.UUID;

public interface PositionService {

    List<PositionResponse> getAll();

    PositionResponse getById(UUID id);

    void updatePositionFromExecution(TradeExecution execution);

    PositionResponse rebuildPosition(UUID positionId);

    List<PositionResponse> getPositionsByPortfolioId(UUID portfolioId);
}
