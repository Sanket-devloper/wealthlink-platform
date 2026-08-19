package com.wealthlink.reconciliation.controller;

import com.wealthlink.reconciliation.dto.request.IngestExternalRecordRequest;
import com.wealthlink.reconciliation.dto.request.ResolveReconciliationItemRequest;
import com.wealthlink.reconciliation.dto.request.StartReconciliationRunRequest;
import com.wealthlink.reconciliation.dto.response.ReconciliationItemResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationResolutionResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationRunResponse;
import com.wealthlink.reconciliation.entity.ReconciliationMatchStatus;
import com.wealthlink.reconciliation.service.ReconciliationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/runs")
    public ResponseEntity<ReconciliationRunResponse> startRun(@Valid @RequestBody StartReconciliationRunRequest request) {
        return new ResponseEntity<>(reconciliationService.startRun(request), HttpStatus.CREATED);
    }

    @PostMapping("/runs/{runId}/complete")
    public ResponseEntity<ReconciliationRunResponse> completeRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(reconciliationService.completeRun(runId));
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<ReconciliationRunResponse> getRunById(@PathVariable UUID runId) {
        return ResponseEntity.ok(reconciliationService.getRunById(runId));
    }

    @GetMapping("/runs")
    public ResponseEntity<List<ReconciliationRunResponse>> getRunsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reconciliationService.getRunsByBusinessDate(date));
    }

    @PostMapping("/external-records")
    public ResponseEntity<Void> ingestExternalRecord(@Valid @RequestBody IngestExternalRecordRequest request) {
        reconciliationService.ingestExternalRecord(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/runs/{runId}/items")
    public ResponseEntity<List<ReconciliationItemResponse>> getItems(
            @PathVariable UUID runId,
            @RequestParam(name = "status", required = false) ReconciliationMatchStatus matchStatus) {
        return ResponseEntity.ok(reconciliationService.getItemsByRunAndStatus(runId, matchStatus));
    }

    @PostMapping("/items/{itemId}/resolve")
    public ResponseEntity<ReconciliationResolutionResponse> resolveItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody ResolveReconciliationItemRequest request) {
        return ResponseEntity.ok(reconciliationService.resolveItem(itemId, request));
    }
}