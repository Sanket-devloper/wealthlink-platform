package com.wealthlink.trade.repository;

import com.wealthlink.trade.entity.TradeExecution;
import com.wealthlink.trade.entity.TradeExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeExecutionRepository extends JpaRepository<TradeExecution, UUID> {
    Optional<TradeExecution> findByExecutionReference(String executionReference);
    List<TradeExecution> findByTradeOrderId(UUID tradeOrderId);
    List<TradeExecution> findByTradeOrderIdAndStatus(UUID tradeOrderId, TradeExecutionStatus status);
}
