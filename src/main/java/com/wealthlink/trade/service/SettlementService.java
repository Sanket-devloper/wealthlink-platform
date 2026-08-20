package com.wealthlink.trade.service;

import com.wealthlink.trade.dto.CreateSettlementRequest;
import com.wealthlink.trade.dto.RetrySettlementResponse;
import com.wealthlink.trade.dto.SettlementResponse;

import java.util.UUID;

public interface SettlementService {

    SettlementResponse createSettlement(UUID executionId, CreateSettlementRequest request);

    RetrySettlementResponse retrySettlement(UUID settlementId);

    SettlementResponse getSettlementById(UUID settlementId);
}
