package com.wealthlink.marketdata.controller;

import com.wealthlink.marketdata.dto.FundPriceCreateRequest;
import com.wealthlink.marketdata.dto.FundPriceResponse;
import com.wealthlink.marketdata.service.FundPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fund-prices")
@RequiredArgsConstructor
public class FundPriceController {

    private final FundPriceService fundPriceService;

    @PostMapping
    public ResponseEntity<FundPriceResponse> create(
            @Valid @RequestBody FundPriceCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundPriceService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FundPriceResponse>> getAll() {

        return ResponseEntity.ok(fundPriceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FundPriceResponse> getById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(fundPriceService.getById(id));
    }

    @GetMapping("/share-class/{fundShareClassId}")
    public ResponseEntity<List<FundPriceResponse>> getByFundShareClass(
            @PathVariable("fundShareClassId") UUID fundShareClassId) {

        return ResponseEntity.ok(
                fundPriceService.getByFundShareClass(fundShareClassId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id) {

        fundPriceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}