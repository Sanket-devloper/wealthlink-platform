package com.wealthlink.importdata.service;

import com.wealthlink.importdata.dto.ImportItemCreateRequest;
import com.wealthlink.importdata.dto.ImportItemResponse;
import com.wealthlink.importdata.entity.ImportItemStatus;

import java.util.List;
import java.util.UUID;

public interface ImportItemService {

    ImportItemResponse createImportItem(
            ImportItemCreateRequest request
    );

    ImportItemResponse getImportItemById(UUID id);

    List<ImportItemResponse> getAllImportItems();

    List<ImportItemResponse> getImportItemsByImportBatch(
            UUID importBatchId
    );

    List<ImportItemResponse> getImportItemsByStatus(
            UUID importBatchId,
            ImportItemStatus status
    );

    void deleteImportItem(UUID id);
}