package com.wealthlink.importdata.service.impl;

import com.wealthlink.importdata.entity.ImportItem;
import com.wealthlink.importdata.entity.ImportItemStatus;
import com.wealthlink.importdata.repository.ImportItemRepository;
import com.wealthlink.importdata.service.ImportItemProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportItemProcessorServiceImpl
        implements ImportItemProcessorService {

    private final ImportItemRepository importItemRepository;

    /**
     * Each import item is processed in its own transaction.
     *
     * A failure in one import item must not rollback
     * successful items from the same import batch.
     *
     * Later, this transaction will contain:
     *
     * IMPORT_ITEM
     *      +
     * FUND_PRICE
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processItem(UUID importItemId) {

        ImportItem item = importItemRepository
                .findById(importItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Import item not found: " + importItemId
                        ));

        try {

            /*
             * Actual provider payload parsing and
             * FundPrice creation will be implemented later.
             *
             * For now, this establishes the required
             * per-item transaction boundary.
             */

            item.setStatus(ImportItemStatus.SUCCESS);
            item.setErrorDetails(null);
            item.setProcessedAt(Instant.now());

            importItemRepository.save(item);

        } catch (Exception ex) {

            item.setStatus(ImportItemStatus.FAILED);
            item.setErrorDetails(ex.getMessage());
            item.setProcessedAt(Instant.now());

            importItemRepository.save(item);

            /*
             * Do not rethrow the exception.
             *
             * This allows the FAILED status and error details
             * to be committed instead of rolling back.
             */
        }
    }
}