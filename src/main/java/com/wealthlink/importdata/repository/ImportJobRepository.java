package com.wealthlink.importdata.repository;

import com.wealthlink.importdata.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
    List<ImportJob> findByProviderId(UUID providerId);
}
