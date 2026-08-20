package com.wealthlink.importdata.service.impl;

import com.wealthlink.importdata.dto.ImportBatchCreateRequest;
import com.wealthlink.importdata.dto.ImportBatchResponse;
import com.wealthlink.importdata.entity.ImportBatch;
import com.wealthlink.importdata.entity.ImportJob;
import com.wealthlink.importdata.exception.ImportBatchNotFoundException;
import com.wealthlink.importdata.mapper.ImportBatchMapper;
import com.wealthlink.importdata.repository.ImportBatchRepository;
import com.wealthlink.importdata.repository.ImportJobRepository;
import com.wealthlink.importdata.service.ImportBatchService;
import lombok.RequiredArgsConstructor;
import com.wealthlink.importdata.entity.ImportBatchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportBatchServiceImpl implements ImportBatchService {

    private final ImportBatchRepository importBatchRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportBatchMapper importBatchMapper;

    @Override
    public ImportBatchResponse createImportBatch(
            ImportBatchCreateRequest request) {

        // 1. Idempotency check
        return importBatchRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .map(importBatchMapper::toResponse)
                .orElseGet(() -> {

                    // 2. Validate Import Job
                    ImportJob importJob = importJobRepository
                            .findById(request.importJobId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Import job not found with id: "
                                                    + request.importJobId()
                                    ));

                    // 3. Create new batch
                    ImportBatch importBatch = ImportBatch.builder()
                            .importJob(importJob)
                            .idempotencyKey(request.idempotencyKey())
                            .status(ImportBatchStatus.RUNNING)
                            .totalItems(0)
                            .successCount(0)
                            .failureCount(0)
                            .build();

                    // 4. Persist
                    ImportBatch savedImportBatch =
                            importBatchRepository.saveAndFlush(importBatch);

                    return importBatchMapper.toResponse(savedImportBatch);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ImportBatchResponse getImportBatchById(UUID id) {

        ImportBatch importBatch = importBatchRepository
                .findById(id)
                .orElseThrow(() ->
                        new ImportBatchNotFoundException(id));

        return importBatchMapper.toResponse(importBatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportBatchResponse> getAllImportBatches() {

        return importBatchRepository.findAll()
                .stream()
                .map(importBatchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportBatchResponse> getImportBatchesByImportJob(
            UUID importJobId) {

        return importBatchRepository
                .findByImportJobId(importJobId)
                .stream()
                .map(importBatchMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteImportBatch(UUID id) {

        ImportBatch importBatch = importBatchRepository
                .findById(id)
                .orElseThrow(() ->
                        new ImportBatchNotFoundException(id));

        importBatchRepository.delete(importBatch);
    }

    @Override
    public void retryImportBatch(UUID batchId) {

        ImportBatch batch = importBatchRepository
                .findById(batchId)
                .orElseThrow(() ->
                        new ImportBatchNotFoundException(batchId));

        if (batch.getStatus() == ImportBatchStatus.RUNNING) {

            throw new IllegalStateException(
                    "Import batch is already running"
            );
        }

        batch.setStatus(ImportBatchStatus.RUNNING);
        batch.setCompletedAt(null);

        importBatchRepository.save(batch);
    }
}