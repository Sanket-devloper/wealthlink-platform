package com.wealthlink.importdata.mapper;

import com.wealthlink.importdata.dto.ImportItemResponse;
import com.wealthlink.importdata.entity.ImportItem;
import org.springframework.stereotype.Component;

@Component
public class ImportItemMapper {

    public ImportItemResponse toResponse(ImportItem importItem) {

        return new ImportItemResponse(
                importItem.getId(),
                importItem.getImportBatch().getId(),
                importItem.getRawPayload(),
                importItem.getStatus(),
                importItem.getErrorDetails(),
                importItem.getFundPrice() != null
                        ? importItem.getFundPrice().getId()
                        : null,
                importItem.getProcessedAt()
        );
    }
}