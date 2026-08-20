package com.wealthlink.importdata.service;

import com.wealthlink.importdata.dto.ImportJobCreateRequest;
import com.wealthlink.importdata.dto.ImportJobResponse;

import java.util.List;
import java.util.UUID;

public interface ImportJobService {

    ImportJobResponse createImportJob(ImportJobCreateRequest request);

    ImportJobResponse getImportJobById(UUID id);

    List<ImportJobResponse> getAllImportJobs();

    List<ImportJobResponse> getImportJobsByProvider(UUID providerId);

    void deleteImportJob(UUID id);
}