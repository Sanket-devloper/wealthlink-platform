package com.wealthlink.trade.service;

import com.wealthlink.trade.dto.ExecutionResponse;
import com.wealthlink.trade.dto.RecordExecutionRequest;

import java.util.List;
import java.util.UUID;

public interface TradeExecutionService {

    List<ExecutionResponse> getExecutionsByOrderId(UUID orderId);

    ExecutionResponse getById(UUID id);

    ExecutionResponse recordExecution(UUID orderId, RecordExecutionRequest request);
}
