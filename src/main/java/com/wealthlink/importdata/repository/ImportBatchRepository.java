package com.wealthlink.importdata.repository;

import com.wealthlink.importdata.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {

    Optional<ImportBatch> findByIdempotencyKey(String idempotencyKey);

    List<ImportBatch> findByImportJobId(UUID importJobId);
}