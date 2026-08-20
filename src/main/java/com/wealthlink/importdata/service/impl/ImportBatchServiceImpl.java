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

        /*
         * Idempotency check.
         *
         * If the same key was already used, return the
         * existing batch instead of creating a duplicate.
         */
        return importBatchRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .map(importBatchMapper::toResponse)
                .orElseGet(() -> {

                    ImportJob importJob = importJobRepository
                            .findById(request.importJobId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Import job not found with id: "
                                                    + request.importJobId()
                                    ));

                    ImportBatch importBatch = ImportBatch.builder()
                            .importJob(importJob)
                            .idempotencyKey(request.idempotencyKey())
                            .build();

                    ImportBatch savedImportBatch =
                            importBatchRepository.save(importBatch);

                    return importBatchMapper.toResponse(
                            savedImportBatch
                    );
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
}