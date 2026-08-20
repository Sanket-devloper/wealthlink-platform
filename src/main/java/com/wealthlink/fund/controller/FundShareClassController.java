package com.wealthlink.fund.controller;

import com.wealthlink.fund.dto.FundShareClassCreateRequest;
import com.wealthlink.fund.dto.FundShareClassResponse;
import com.wealthlink.fund.dto.FundShareClassUpdateRequest;
import com.wealthlink.fund.service.FundShareClassService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fund-share-classes")
public class FundShareClassController {

    private final FundShareClassService fundShareClassService;

    public FundShareClassController(
            FundShareClassService fundShareClassService
    ) {
        this.fundShareClassService = fundShareClassService;
    }

    @PostMapping
    public ResponseEntity<FundShareClassResponse> createShareClass(
            @Valid @RequestBody FundShareClassCreateRequest request
    ) {

        FundShareClassResponse response =
                fundShareClassService.createShareClass(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{shareClassId}")
    public ResponseEntity<FundShareClassResponse> getShareClassById(
            @PathVariable("shareClassId") UUID shareClassId
    ) {

        return ResponseEntity.ok(
                fundShareClassService.getShareClassById(shareClassId)
        );
    }

    @GetMapping
    public ResponseEntity<List<FundShareClassResponse>> getShareClasses(
            @RequestParam(value = "fundId", required = false) UUID fundId
    ) {

        if (fundId != null) {
            return ResponseEntity.ok(
                    fundShareClassService.getShareClassesByFundId(fundId)
            );
        }

        return ResponseEntity.ok(
                fundShareClassService.getAllShareClasses()
        );
    }

    @PatchMapping("/{shareClassId}")
    public ResponseEntity<FundShareClassResponse> updateShareClass(
            @PathVariable("shareClassId") UUID shareClassId,
            @Valid @RequestBody FundShareClassUpdateRequest request
    ) {

        return ResponseEntity.ok(
                fundShareClassService.updateShareClass(
                        shareClassId,
                        request
                )
        );
    }
}