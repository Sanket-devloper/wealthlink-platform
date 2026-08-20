package com.wealthlink.importdata.mapper;

import com.wealthlink.importdata.dto.ImportBatchResponse;
import com.wealthlink.importdata.entity.ImportBatch;
import org.springframework.stereotype.Component;

@Component
public class ImportBatchMapper {

    public ImportBatchResponse toResponse(ImportBatch importBatch) {

        return new ImportBatchResponse(
                importBatch.getId(),
                importBatch.getImportJob().getId(),
                importBatch.getIdempotencyKey(),
                importBatch.getStatus(),
                importBatch.getStartedAt(),
                importBatch.getCompletedAt(),
                importBatch.getTotalItems(),
                importBatch.getSuccessCount(),
                importBatch.getFailureCount(),
                importBatch.getVersion()
        );
    }
}