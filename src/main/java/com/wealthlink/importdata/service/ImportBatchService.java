package com.wealthlink.importdata.service;

import com.wealthlink.importdata.dto.ImportBatchCreateRequest;
import com.wealthlink.importdata.dto.ImportBatchResponse;

import java.util.List;
import java.util.UUID;

public interface ImportBatchService {

    ImportBatchResponse createImportBatch(
            ImportBatchCreateRequest request
    );

    ImportBatchResponse getImportBatchById(UUID id);

    List<ImportBatchResponse> getAllImportBatches();

    List<ImportBatchResponse> getImportBatchesByImportJob(UUID importJobId);

    void deleteImportBatch(UUID id);
}