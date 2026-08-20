package com.wealthlink.trade.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.wealthlink.trade.dto.CreateSettlementRequest;
import com.wealthlink.trade.dto.RetrySettlementResponse;
import com.wealthlink.trade.dto.SettlementResponse;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Settlement APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final com.wealthlink.trade.service.SettlementService settlementService;

    @PostMapping("/api/v1/trade-executions/{executionId}/settlement")
    
    public ResponseEntity<SettlementResponse> createSettlement(@PathVariable UUID executionId, @RequestBody CreateSettlementRequest payload) {
        return ResponseEntity.ok(settlementService.createSettlement(executionId, payload));
    }

    @GetMapping("/api/v1/settlements/{settlementId}")
    
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable UUID settlementId) {
        return ResponseEntity.ok().build(); // TODO: implement getById
    }

    @PostMapping("/api/v1/settlements/{settlementId}/complete")
    
    public ResponseEntity<RetrySettlementResponse> completeSettlement(@PathVariable UUID settlementId, @RequestBody Object payload) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

    @GetMapping("/api/v1/settlements/{settlementId}/status")
    
    public ResponseEntity<Object> getSettlementStatus(@PathVariable UUID settlementId) {
        return ResponseEntity.ok().build(); // TODO: Delegate to Service
    }

}
