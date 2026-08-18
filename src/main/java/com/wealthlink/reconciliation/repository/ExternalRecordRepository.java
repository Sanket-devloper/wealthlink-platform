package com.wealthlink.reconciliation.repository;

import com.wealthlink.reconciliation.entity.ExternalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalRecordRepository extends JpaRepository<ExternalRecord, UUID> {
    List<ExternalRecord> findByReconciliationRunId(UUID runId);
    Optional<ExternalRecord> findByExternalReference(String externalReference);
}