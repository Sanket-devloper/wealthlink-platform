package com.wealthlink.importdata.service.impl;

import com.wealthlink.importdata.dto.ImportItemCreateRequest;
import com.wealthlink.importdata.dto.ImportItemResponse;
import com.wealthlink.importdata.entity.ImportBatch;
import com.wealthlink.importdata.entity.ImportItem;
import com.wealthlink.importdata.entity.ImportItemStatus;
import com.wealthlink.importdata.exception.ImportItemNotFoundException;
import com.wealthlink.importdata.mapper.ImportItemMapper;
import com.wealthlink.importdata.repository.ImportBatchRepository;
import com.wealthlink.importdata.repository.ImportItemRepository;
import com.wealthlink.importdata.service.ImportItemProcessorService;
import com.wealthlink.importdata.service.ImportItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportItemServiceImpl implements ImportItemService {

    private final ImportItemRepository importItemRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ImportItemMapper importItemMapper;
    private final ImportItemProcessorService importItemProcessorService;

    @Override
    public ImportItemResponse createImportItem(
            ImportItemCreateRequest request) {

        ImportBatch importBatch = importBatchRepository
                .findById(request.importBatchId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Import batch not found with id: "
                                        + request.importBatchId()
                        ));

        ImportItem importItem = ImportItem.builder()
                .importBatch(importBatch)
                .rawPayload(request.rawPayload())
                .build();

        ImportItem savedImportItem =
                importItemRepository.save(importItem);

        return importItemMapper.toResponse(savedImportItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportItemResponse getImportItemById(UUID id) {

        ImportItem importItem = importItemRepository
                .findById(id)
                .orElseThrow(() ->
                        new ImportItemNotFoundException(id));

        return importItemMapper.toResponse(importItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportItemResponse> getAllImportItems() {

        return importItemRepository.findAll()
                .stream()
                .map(importItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportItemResponse> getImportItemsByImportBatch(
            UUID importBatchId) {

        return importItemRepository
                .findByImportBatchId(importBatchId)
                .stream()
                .map(importItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportItemResponse> getImportItemsByStatus(
            UUID importBatchId,
            ImportItemStatus status) {

        return importItemRepository
                .findByImportBatchIdAndStatus(
                        importBatchId,
                        status
                )
                .stream()
                .map(importItemMapper::toResponse)
                .toList();
    }

    @Override
    public void retryImportItem(UUID id) {

        ImportItem importItem = importItemRepository
                .findById(id)
                .orElseThrow(() ->
                        new ImportItemNotFoundException(id));

        if (importItem.getStatus() != ImportItemStatus.FAILED) {

            throw new IllegalStateException(
                    "Only FAILED import items can be retried"
            );
        }

        importItem.setStatus(ImportItemStatus.PENDING);
        importItem.setErrorDetails(null);
        importItem.setProcessedAt(null);

        importItemRepository.save(importItem);

        importItemProcessorService.processItem(id);
    }

    @Override
    public void deleteImportItem(UUID id) {

        ImportItem importItem = importItemRepository
                .findById(id)
                .orElseThrow(() ->
                        new ImportItemNotFoundException(id));

        importItemRepository.delete(importItem);
    }
}