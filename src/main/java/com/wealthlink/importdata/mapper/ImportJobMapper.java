package com.wealthlink.importdata.mapper;

import com.wealthlink.importdata.dto.ImportJobResponse;
import com.wealthlink.importdata.entity.ImportJob;
import org.springframework.stereotype.Component;

@Component
public class ImportJobMapper {

    public ImportJobResponse toResponse(ImportJob importJob) {

        return new ImportJobResponse(
                importJob.getId(),
                importJob.getName(),
                importJob.getProvider().getId(),
                importJob.getJobType(),
                importJob.getStatus(),
                importJob.getCreatedAt()
        );
    }
}