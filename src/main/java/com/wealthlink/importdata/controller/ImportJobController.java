package com.wealthlink.importdata.controller;

import com.wealthlink.importdata.dto.ImportJobCreateRequest;
import com.wealthlink.importdata.dto.ImportJobResponse;
import com.wealthlink.importdata.service.ImportJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import-jobs")
@RequiredArgsConstructor
public class ImportJobController {

    private final ImportJobService importJobService;

    @PostMapping
    public ResponseEntity<ImportJobResponse> createImportJob(
            @Valid @RequestBody ImportJobCreateRequest request) {

        ImportJobResponse response =
                importJobService.createImportJob(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ImportJobResponse>> getAllImportJobs() {

        return ResponseEntity.ok(
                importJobService.getAllImportJobs()
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ImportJobResponse>> getImportJobsByProvider(
            @PathVariable("providerId") UUID providerId) {

        return ResponseEntity.ok(
                importJobService.getImportJobsByProvider(providerId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportJobResponse> getImportJobById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                importJobService.getImportJobById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImportJob(
            @PathVariable("id") UUID id) {

        importJobService.deleteImportJob(id);

        return ResponseEntity.noContent().build();
    }
}