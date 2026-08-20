package com.wealthlink.importdata.controller;

import com.wealthlink.importdata.dto.ImportBatchCreateRequest;
import com.wealthlink.importdata.dto.ImportBatchResponse;
import com.wealthlink.importdata.service.ImportBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/import-batches")
@RequiredArgsConstructor
public class ImportBatchController {

    private final ImportBatchService importBatchService;

    @PostMapping
    public ResponseEntity<ImportBatchResponse> createImportBatch(
            @Valid @RequestBody ImportBatchCreateRequest request) {

        ImportBatchResponse response =
                importBatchService.createImportBatch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ImportBatchResponse>> getAllImportBatches() {

        return ResponseEntity.ok(
                importBatchService.getAllImportBatches()
        );
    }

    @GetMapping("/job/{importJobId}")
    public ResponseEntity<List<ImportBatchResponse>> getImportBatchesByImportJob(
            @PathVariable("importJobId") UUID importJobId) {

        return ResponseEntity.ok(
                importBatchService.getImportBatchesByImportJob(
                        importJobId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportBatchResponse> getImportBatchById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                importBatchService.getImportBatchById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImportBatch(
            @PathVariable("id") UUID id) {

        importBatchService.deleteImportBatch(id);

        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryImportBatch(
            @PathVariable("id") UUID id) {

        importBatchService.retryImportBatch(id);

        return ResponseEntity.accepted().build();
    }

}