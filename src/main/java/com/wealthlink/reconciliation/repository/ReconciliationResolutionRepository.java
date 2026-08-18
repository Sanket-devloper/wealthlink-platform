package com.wealthlink.reconciliation.repository;

import com.wealthlink.reconciliation.entity.ReconciliationResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReconciliationResolutionRepository extends JpaRepository<ReconciliationResolution, UUID> {
    Optional<ReconciliationResolution> findByReconciliationItemId(UUID reconciliationItemId);
}