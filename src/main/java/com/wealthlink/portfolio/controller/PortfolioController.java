package com.wealthlink.portfolio.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.wealthlink.portfolio.dto.*;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Portfolio APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class PortfolioController {

    @PostMapping("/api/v1/portfolios")
    
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestBody CreatePortfolioRequest payload) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @GetMapping("/api/v1/portfolios/{id}")
    
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID id) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @PutMapping("/api/v1/portfolios/{id}")
    
    public ResponseEntity<PortfolioResponse> updatePortfolio(@PathVariable UUID id, @RequestBody Object payload) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @GetMapping("/api/v1/portfolios/{id}/positions")
    
    public ResponseEntity<List<PositionResponse>> listPositions(@PathVariable UUID id) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @PostMapping("/api/v1/portfolios/{id}/valuations")
    
    public ResponseEntity<ValuationResponse> createValuationSnapshot(@PathVariable UUID id) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @GetMapping("/api/v1/portfolios/{id}/valuations/latest")
    
    public ResponseEntity<ValuationResponse> getLatestValuation(@PathVariable UUID id) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @GetMapping("/api/v1/portfolios/{id}/valuations")
    
    public ResponseEntity<List<ValuationResponse>> getValuationHistory(@PathVariable UUID id) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    // This is typically listed under Account APIs in requirements: GET /api/v1/accounts/{accountId}/portfolios
    // We will place it here for convenience, mapped to that exact path
    @GetMapping("/api/v1/accounts/{accountId}/portfolios")
    
    public ResponseEntity<List<PortfolioResponse>> getAccountPortfolios(@PathVariable UUID accountId) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }
}
