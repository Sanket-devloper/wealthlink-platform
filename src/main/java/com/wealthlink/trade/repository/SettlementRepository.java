package com.wealthlink.trade.repository;

import com.wealthlink.trade.entity.Settlement;
import com.wealthlink.trade.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findBySettlementReference(String settlementReference);
    Optional<Settlement> findByTradeExecutionId(UUID tradeExecutionId);
    List<Settlement> findByStatusAndSettlementDateBefore(SettlementStatus status, LocalDate date);
}
