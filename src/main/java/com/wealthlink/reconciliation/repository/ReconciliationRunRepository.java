package com.wealthlink.reconciliation.repository;

import com.wealthlink.reconciliation.entity.ReconciliationRun;
import com.wealthlink.reconciliation.entity.ReconciliationRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {
    List<ReconciliationRun> findByBusinessDate(LocalDate businessDate);
    List<ReconciliationRun> findByStatus(ReconciliationRunStatus status);
}