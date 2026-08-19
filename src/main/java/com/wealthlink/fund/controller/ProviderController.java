package com.wealthlink.fund.controller;

import com.wealthlink.fund.dto.ProviderCreateRequest;
import com.wealthlink.fund.dto.ProviderResponse;
import com.wealthlink.fund.dto.ProviderUpdateRequest;
import com.wealthlink.fund.service.ProviderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(
            ProviderService providerService
    ) {
        this.providerService = providerService;
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(
            @Valid @RequestBody ProviderCreateRequest request
    ) {

        ProviderResponse response =
                providerService.createProvider(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderResponse> getProviderById(
            @PathVariable("providerId") UUID providerId
    ) {

        return ResponseEntity.ok(
                providerService.getProviderById(providerId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {

        return ResponseEntity.ok(
                providerService.getAllProviders()
        );
    }

    @PatchMapping("/{providerId}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable("providerId") UUID providerId,
            @Valid @RequestBody ProviderUpdateRequest request
    ) {

        return ResponseEntity.ok(
                providerService.updateProvider(
                        providerId,
                        request
                )
        );
    }
}