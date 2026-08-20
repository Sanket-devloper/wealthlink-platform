package com.wealthlink.importdata.service.impl;

import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.importdata.dto.ImportJobCreateRequest;
import com.wealthlink.importdata.dto.ImportJobResponse;
import com.wealthlink.importdata.entity.ImportJob;
import com.wealthlink.importdata.exception.ImportJobNotFoundException;
import com.wealthlink.importdata.mapper.ImportJobMapper;
import com.wealthlink.importdata.repository.ImportJobRepository;
import com.wealthlink.importdata.service.ImportJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportJobServiceImpl implements ImportJobService {

    private final ImportJobRepository importJobRepository;
    private final ProviderRepository providerRepository;
    private final ImportJobMapper importJobMapper;

    @Override
    public ImportJobResponse createImportJob(ImportJobCreateRequest request) {

        Provider provider = providerRepository.findById(request.providerId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Provider not found with id: " + request.providerId()
                        ));

        ImportJob importJob = ImportJob.builder()
                .name(request.name())
                .provider(provider)
                .jobType(request.jobType())
                .build();

        ImportJob savedImportJob = importJobRepository.save(importJob);

        return importJobMapper.toResponse(savedImportJob);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportJobResponse getImportJobById(UUID id) {

        ImportJob importJob = importJobRepository.findById(id)
                .orElseThrow(() -> new ImportJobNotFoundException(id));

        return importJobMapper.toResponse(importJob);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportJobResponse> getAllImportJobs() {

        return importJobRepository.findAll()
                .stream()
                .map(importJobMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportJobResponse> getImportJobsByProvider(UUID providerId) {

        return importJobRepository.findByProviderId(providerId)
                .stream()
                .map(importJobMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteImportJob(UUID id) {

        ImportJob importJob = importJobRepository.findById(id)
                .orElseThrow(() -> new ImportJobNotFoundException(id));

        importJobRepository.delete(importJob);
    }
}