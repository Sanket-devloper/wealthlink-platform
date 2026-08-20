package com.wealthlink.trade.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.ExecutionResponse;
import com.wealthlink.trade.dto.RecordExecutionRequest;
import com.wealthlink.trade.entity.TradeExecution;
import com.wealthlink.trade.entity.TradeExecutionStatus;
import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderStatus;
import com.wealthlink.trade.repository.TradeExecutionRepository;
import com.wealthlink.trade.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeExecutionServiceImpl implements TradeExecutionService {

    private final TradeExecutionRepository tradeExecutionRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final CurrencyRepository currencyRepository;

    public List<ExecutionResponse> getExecutionsByOrderId(UUID orderId) {
        return tradeExecutionRepository.findByTradeOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExecutionResponse getById(UUID id) {
        TradeExecution execution = tradeExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TradeExecution", id));
        return mapToResponse(execution);
    }

    @Transactional
    public ExecutionResponse recordExecution(UUID orderId, RecordExecutionRequest request) {
        TradeOrder order = tradeOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("TradeOrder", orderId));
                
        TradeExecution execution = new TradeExecution();
        execution.setTradeOrder(order);
        execution.setExecutionReference(UUID.randomUUID().toString());
        execution.setStatus(TradeExecutionStatus.CONFIRMED);
        execution.setTradeDate(request.getTradeDate());
        execution.setExecutedQuantity(request.getExecutedQuantity());
        execution.setExecutionPrice(request.getExecutedPrice());
        execution.setGrossAmount(request.getGrossAmount());
        execution.setFee(request.getFeeAmount() != null ? request.getFeeAmount() : BigDecimal.ZERO);
        execution.setTax(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        execution.setNetAmount(request.getNetAmount());
        execution.setCurrency(currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId())));
        execution.setExternalReference(request.getExternalReference());
        execution.setExecutedAt(Instant.now());
        
        TradeExecution savedExecution = tradeExecutionRepository.save(execution);
        
        // Check if order is fully filled based on sum of execution quantities
        BigDecimal totalExecuted = tradeExecutionRepository.findByTradeOrderId(order.getId()).stream()
                .map(TradeExecution::getExecutedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        if (totalExecuted.compareTo(order.getRequestedQuantity()) >= 0) {
            order.setStatus(TradeOrderStatus.FILLED);
        } else {
            order.setStatus(TradeOrderStatus.PARTIALLY_FILLED);
        }
        tradeOrderRepository.save(order);
        
        return mapToResponse(savedExecution);
    }

    private ExecutionResponse mapToResponse(TradeExecution execution) {
        return ExecutionResponse.builder()
                .id(execution.getId())
                .orderId(execution.getTradeOrder().getId())
                .executedQuantity(execution.getExecutedQuantity())
                .executedPrice(execution.getExecutionPrice())
                .grossAmount(execution.getGrossAmount())
                .feeAmount(execution.getFee())
                .taxAmount(execution.getTax())
                .netAmount(execution.getNetAmount())
                .executionStatus(execution.getStatus().name())
                .build();
    }
}
