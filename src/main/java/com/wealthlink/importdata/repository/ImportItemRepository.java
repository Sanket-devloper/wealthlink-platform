package com.wealthlink.importdata.repository;

import com.wealthlink.importdata.entity.ImportItem;
import com.wealthlink.importdata.entity.ImportItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportItemRepository extends JpaRepository<ImportItem, UUID> {
    List<ImportItem> findByImportBatchId(UUID importBatchId);

    List<ImportItem> findByImportBatchIdAndStatus(UUID importBatchId, ImportItemStatus status);
}
