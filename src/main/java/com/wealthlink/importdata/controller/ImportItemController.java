package com.wealthlink.importdata.controller;

import com.wealthlink.importdata.dto.ImportItemCreateRequest;
import com.wealthlink.importdata.dto.ImportItemResponse;
import com.wealthlink.importdata.entity.ImportItemStatus;
import com.wealthlink.importdata.service.ImportItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import-items")
@RequiredArgsConstructor
public class ImportItemController {

    private final ImportItemService importItemService;

    @PostMapping
    public ResponseEntity<ImportItemResponse> createImportItem(
            @Valid @RequestBody ImportItemCreateRequest request) {

        ImportItemResponse response =
                importItemService.createImportItem(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ImportItemResponse>> getAllImportItems() {

        return ResponseEntity.ok(
                importItemService.getAllImportItems()
        );
    }

    @GetMapping("/batch/{importBatchId}")
    public ResponseEntity<List<ImportItemResponse>>
    getImportItemsByImportBatch(
            @PathVariable("importBatchId") UUID importBatchId) {

        return ResponseEntity.ok(
                importItemService.getImportItemsByImportBatch(
                        importBatchId
                )
        );
    }

    @GetMapping("/batch/{importBatchId}/status/{status}")
    public ResponseEntity<List<ImportItemResponse>>
    getImportItemsByStatus(
            @PathVariable("importBatchId") UUID importBatchId,
            @PathVariable("status") ImportItemStatus status) {

        return ResponseEntity.ok(
                importItemService.getImportItemsByStatus(
                        importBatchId,
                        status
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportItemResponse> getImportItemById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                importItemService.getImportItemById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImportItem(
            @PathVariable("id") UUID id) {

        importItemService.deleteImportItem(id);

        return ResponseEntity.noContent().build();
    }
}