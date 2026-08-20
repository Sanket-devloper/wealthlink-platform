package com.wealthlink.marketdata.controller;

import com.wealthlink.marketdata.dto.FxRateCreateRequest;
import com.wealthlink.marketdata.dto.FxRateResponse;
import com.wealthlink.marketdata.service.FxRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fx-rates")
@RequiredArgsConstructor
public class FxRateController {

    private final FxRateService fxRateService;

    @PostMapping
    public ResponseEntity<FxRateResponse> create(
            @Valid @RequestBody FxRateCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fxRateService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FxRateResponse>> getAll() {

        return ResponseEntity.ok(fxRateService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FxRateResponse> getById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(fxRateService.getById(id));
    }

    @GetMapping("/currency-pair/{baseCurrencyId}/{quoteCurrencyId}")
    public ResponseEntity<List<FxRateResponse>> getByCurrencyPair(
            @PathVariable("baseCurrencyId") UUID baseCurrencyId,
            @PathVariable("quoteCurrencyId") UUID quoteCurrencyId) {

        return ResponseEntity.ok(
                fxRateService.getByCurrencyPair(
                        baseCurrencyId,
                        quoteCurrencyId
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id) {

        fxRateService.delete(id);

        return ResponseEntity.noContent().build();
    }
}