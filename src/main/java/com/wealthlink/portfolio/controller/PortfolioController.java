package com.wealthlink.portfolio.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.wealthlink.portfolio.dto.*;
import io.swagger.v3.oas.annotations.Parameter;

import com.wealthlink.portfolio.service.PortfolioService;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Portfolio APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final com.wealthlink.portfolio.service.PositionService positionService;
    private final com.wealthlink.portfolio.service.PortfolioValuationSnapshotService valuationService;

    @PostMapping("/api/v1/portfolios")
    
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestBody CreatePortfolioRequest payload) {
        return ResponseEntity.ok(portfolioService.createPortfolio(payload));
    }

    @GetMapping("/api/v1/portfolios/{id}")
    
    public ResponseEntity<PortfolioResponse> getPortfolio(@Parameter(description = "Portfolio ID") @PathVariable UUID id) {
        return ResponseEntity.ok(portfolioService.getById(id));
    }

    @PutMapping("/api/v1/portfolios/{id}")
    
    public ResponseEntity<PortfolioResponse> updatePortfolio(@Parameter(description = "Portfolio ID") @PathVariable UUID id, @RequestBody UpdatePortfolioRequest payload) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, payload));
    }

    @GetMapping("/api/v1/portfolios/{id}/positions")
    
    public ResponseEntity<List<PositionResponse>> listPositions(@Parameter(description = "Portfolio ID") @PathVariable UUID id) {
        return ResponseEntity.ok(positionService.getPositionsByPortfolioId(id));
    }

    @PostMapping("/api/v1/portfolios/{id}/valuations")
    
    public ResponseEntity<ValuationResponse> createValuationSnapshot(@Parameter(description = "Portfolio ID") @PathVariable UUID id) {
        return ResponseEntity.ok(valuationService.createSnapshot(id, java.time.LocalDate.now()));
    }

    @GetMapping("/api/v1/portfolios/{id}/valuations/latest")
    
    public ResponseEntity<ValuationResponse> getLatestValuation(@Parameter(description = "Portfolio ID") @PathVariable UUID id) {
        return ResponseEntity.ok(valuationService.getLatestValuation(id));
    }

    @GetMapping("/api/v1/portfolios/{id}/valuations")
    
    public ResponseEntity<List<ValuationResponse>> getValuationHistory(@Parameter(description = "Portfolio ID") @PathVariable UUID id) {
        return ResponseEntity.ok(valuationService.getValuationHistory(id));
    }

    // This is typically listed under Account APIs in requirements: GET /api/v1/accounts/{accountId}/portfolios
    // We will place it here for convenience, mapped to that exact path
    @GetMapping("/api/v1/accounts/{accountId}/portfolios")
    
    public ResponseEntity<List<PortfolioResponse>> getAccountPortfolios(@Parameter(description = "Account ID") @PathVariable UUID accountId) {
        return ResponseEntity.ok(portfolioService.getPortfoliosByAccountId(accountId));
    }
}
