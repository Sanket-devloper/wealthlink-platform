package com.wealthlink.reconciliation.service;

import com.wealthlink.reconciliation.dto.request.IngestExternalRecordRequest;
import com.wealthlink.reconciliation.dto.request.ResolveReconciliationItemRequest;
import com.wealthlink.reconciliation.dto.request.StartReconciliationRunRequest;
import com.wealthlink.reconciliation.dto.response.ReconciliationItemResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationResolutionResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationRunResponse;
import com.wealthlink.reconciliation.entity.ReconciliationMatchStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReconciliationService {
    ReconciliationRunResponse startRun(StartReconciliationRunRequest request);
    ReconciliationRunResponse completeRun(UUID runId);
    ReconciliationRunResponse getRunById(UUID runId);
    List<ReconciliationRunResponse> getRunsByBusinessDate(LocalDate date);

    void ingestExternalRecord(IngestExternalRecordRequest request);
    List<ReconciliationItemResponse> getItemsByRunAndStatus(UUID runId, ReconciliationMatchStatus matchStatus);
    ReconciliationResolutionResponse resolveItem(UUID itemId, ResolveReconciliationItemRequest request);
}