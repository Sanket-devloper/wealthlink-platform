package com.wealthlink.marketdata.controller;

import com.wealthlink.marketdata.dto.FxRateSourceCreateRequest;
import com.wealthlink.marketdata.dto.FxRateSourceResponse;
import com.wealthlink.marketdata.service.FxRateSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fx-rate-sources")
@RequiredArgsConstructor
public class FxRateSourceController {

    private final FxRateSourceService fxRateSourceService;

    @PostMapping
    public ResponseEntity<FxRateSourceResponse> create(
            @Valid @RequestBody FxRateSourceCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fxRateSourceService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FxRateSourceResponse>> getAll() {

        return ResponseEntity.ok(
                fxRateSourceService.getAll()
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<FxRateSourceResponse> getByCode(
            @PathVariable("code") String code) {

        return ResponseEntity.ok(
                fxRateSourceService.getByCode(code)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FxRateSourceResponse> getById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                fxRateSourceService.getById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id) {

        fxRateSourceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}