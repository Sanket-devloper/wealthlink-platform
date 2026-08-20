package com.wealthlink.fund.controller;

import com.wealthlink.fund.dto.FundProviderMappingCreateRequest;
import com.wealthlink.fund.dto.FundProviderMappingResponse;
import com.wealthlink.fund.service.FundProviderMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fund-provider-mappings")
@RequiredArgsConstructor
public class FundProviderMappingController {

    private final FundProviderMappingService fundProviderMappingService;

    @PostMapping
    public ResponseEntity<FundProviderMappingResponse> create(
            @Valid @RequestBody FundProviderMappingCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundProviderMappingService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FundProviderMappingResponse>> getAll() {

        return ResponseEntity.ok(
                fundProviderMappingService.getAll()
        );
    }

    @GetMapping("/provider/{providerId}/external/{externalFundId}")
    public ResponseEntity<FundProviderMappingResponse>
    getByProviderAndExternalFundId(
            @PathVariable("providerId") UUID providerId,
            @PathVariable("externalFundId") String externalFundId) {

        return ResponseEntity.ok(
                fundProviderMappingService
                        .getByProviderAndExternalFundId(
                                providerId,
                                externalFundId
                        )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FundProviderMappingResponse> getById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                fundProviderMappingService.getById(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id) {

        fundProviderMappingService.delete(id);

        return ResponseEntity.noContent().build();
    }
}