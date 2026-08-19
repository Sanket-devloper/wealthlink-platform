package com.wealthlink.reconciliation.service.impl;

import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.reconciliation.dto.request.IngestExternalRecordRequest;
import com.wealthlink.reconciliation.dto.request.ResolveReconciliationItemRequest;
import com.wealthlink.reconciliation.dto.request.StartReconciliationRunRequest;
import com.wealthlink.reconciliation.dto.response.ReconciliationItemResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationResolutionResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationRunResponse;
import com.wealthlink.reconciliation.entity.*;
import com.wealthlink.reconciliation.exception.InvalidReconciliationStateException;
import com.wealthlink.reconciliation.exception.ReconciliationNotFoundException;
import com.wealthlink.reconciliation.repository.ExternalRecordRepository;
import com.wealthlink.reconciliation.repository.ReconciliationItemRepository;
import com.wealthlink.reconciliation.repository.ReconciliationResolutionRepository;
import com.wealthlink.reconciliation.repository.ReconciliationRunRepository;
import com.wealthlink.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

    private final ReconciliationRunRepository runRepository;
    private final ExternalRecordRepository externalRecordRepository;
    private final ReconciliationItemRepository itemRepository;
    private final ReconciliationResolutionRepository resolutionRepository;
    private final AppUserRepository appUserRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public ReconciliationRunResponse startRun(StartReconciliationRunRequest request) {
        log.info("Starting reconciliation run: type={}, date={}", request.getRunType(), request.getBusinessDate());

        AppUser initiator = null;
        if (request.getInitiatedById() != null) {
            initiator = appUserRepository.findById(request.getInitiatedById()).orElse(null);
        }

        ReconciliationRun run = ReconciliationRun.builder()
                .runType(request.getRunType())
                .businessDate(request.getBusinessDate())
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .initiatedBy(initiator)
                .build();

        return mapToRunResponse(runRepository.save(run));
    }

    @Override
    @Transactional
    public ReconciliationRunResponse completeRun(UUID runId) {
        ReconciliationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation run not found with ID: " + runId));

        if (run.getStatus() == ReconciliationRunStatus.COMPLETED) {
            throw new InvalidReconciliationStateException("Reconciliation run is already completed");
        }

        run.setStatus(ReconciliationRunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        return mapToRunResponse(runRepository.save(run));
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationRunResponse getRunById(UUID runId) {
        return runRepository.findById(runId)
                .map(this::mapToRunResponse)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation run not found: " + runId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationRunResponse> getRunsByBusinessDate(LocalDate date) {
        return runRepository.findByBusinessDate(date).stream()
                .map(this::mapToRunResponse)
                .toList();
    }

    @Override
    @Transactional
    public void ingestExternalRecord(IngestExternalRecordRequest request) {
        ReconciliationRun run = runRepository.findById(request.getReconciliationRunId())
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation run not found: " + request.getReconciliationRunId()));

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ReconciliationNotFoundException("Provider not found: " + request.getProviderId()));

        ExternalRecord record = ExternalRecord.builder()
                .reconciliationRun(run)
                .provider(provider)
                .externalReference(request.getExternalReference())
                .rawPayload(request.getRawPayload())
                .build();

        externalRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationItemResponse> getItemsByRunAndStatus(UUID runId, ReconciliationMatchStatus matchStatus) {
        if (matchStatus != null) {
            return itemRepository.findByReconciliationRunIdAndMatchStatus(runId, matchStatus).stream()
                    .map(this::mapToItemResponse)
                    .toList();
        }
        return itemRepository.findByReconciliationRunId(runId).stream()
                .map(this::mapToItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReconciliationResolutionResponse resolveItem(UUID itemId, ResolveReconciliationItemRequest request) {
        log.info("Resolving reconciliation item {} with type {}", itemId, request.getResolutionType());

        ReconciliationItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation item not found: " + itemId));

        if (resolutionRepository.findByReconciliationItemId(itemId).isPresent()) {
            throw new InvalidReconciliationStateException("Reconciliation item has already been resolved");
        }

        AppUser resolver = appUserRepository.findById(request.getResolvedById())
                .orElseThrow(() -> new ReconciliationNotFoundException("User not found: " + request.getResolvedById()));

        ReconciliationResolution resolution = ReconciliationResolution.builder()
                .reconciliationItem(item)
                .resolvedBy(resolver)
                .resolutionType(request.getResolutionType())
                .resolutionNotes(request.getResolutionNotes())
                .build();

        ReconciliationResolution saved = resolutionRepository.save(resolution);
        return ReconciliationResolutionResponse.builder()
                .id(saved.getId())
                .reconciliationItemId(item.getId())
                .resolvedById(resolver.getId())
                .resolvedByUsername(resolver.getUsername())
                .resolutionType(saved.getResolutionType())
                .resolutionNotes(saved.getResolutionNotes())
                .resolvedAt(saved.getResolvedAt())
                .build();
    }

    private ReconciliationRunResponse mapToRunResponse(ReconciliationRun run) {
        return ReconciliationRunResponse.builder()
                .id(run.getId())
                .runType(run.getRunType())
                .businessDate(run.getBusinessDate())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .status(run.getStatus())
                .initiatedById(run.getInitiatedBy() != null ? run.getInitiatedBy().getId() : null)
                .initiatedByUsername(run.getInitiatedBy() != null ? run.getInitiatedBy().getUsername() : null)
                .build();
    }

    private ReconciliationItemResponse mapToItemResponse(ReconciliationItem item) {
        return ReconciliationItemResponse.builder()
                .id(item.getId())
                .reconciliationRunId(item.getReconciliationRun().getId())
                .externalRecordId(item.getExternalRecord() != null ? item.getExternalRecord().getId() : null)
                .externalReference(item.getExternalRecord() != null ? item.getExternalRecord().getExternalReference() : null)
                .internalReferenceType(item.getInternalReferenceType())
                .internalReferenceId(item.getInternalReferenceId())
                .matchStatus(item.getMatchStatus())
                .differenceDetails(item.getDifferenceDetails())
                .createdAt(item.getCreatedAt())
                .build();
    }
}