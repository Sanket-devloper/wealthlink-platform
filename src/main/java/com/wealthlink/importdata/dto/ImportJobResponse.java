package com.wealthlink.importdata.dto;

import com.wealthlink.importdata.entity.ImportJobStatus;
import com.wealthlink.importdata.entity.ImportJobType;

import java.time.Instant;
import java.util.UUID;

public record ImportJobResponse(

        UUID id,

        String name,

        UUID providerId,

        ImportJobType jobType,

        ImportJobStatus status,

        Instant createdAt

) {
}