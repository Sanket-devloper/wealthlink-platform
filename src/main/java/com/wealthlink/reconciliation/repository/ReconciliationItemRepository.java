package com.wealthlink.reconciliation.repository;

import com.wealthlink.reconciliation.entity.ReconciliationItem;
import com.wealthlink.reconciliation.entity.ReconciliationMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {
    List<ReconciliationItem> findByReconciliationRunId(UUID runId);
    List<ReconciliationItem> findByReconciliationRunIdAndMatchStatus(UUID runId, ReconciliationMatchStatus status);
}