package com.wealthlink.trade.repository;

import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, UUID> {
    Optional<TradeOrder> findByOrderReference(String orderReference);
    List<TradeOrder> findByPortfolioIdAndStatus(UUID portfolioId, TradeOrderStatus status);
    List<TradeOrder> findByPortfolioId(UUID portfolioId);
    Optional<TradeOrder> findByIdempotencyKey(String idempotencyKey);
}
