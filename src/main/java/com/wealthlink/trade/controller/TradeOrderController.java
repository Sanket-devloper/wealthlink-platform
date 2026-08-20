package com.wealthlink.trade.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import com.wealthlink.trade.dto.CancelOrderRequest;
import com.wealthlink.trade.dto.CancelOrderResponse;
import com.wealthlink.trade.dto.CreateOrderRequest;
import com.wealthlink.trade.dto.OrderResponse;

import com.wealthlink.trade.service.TradeOrderService;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Trading APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @GetMapping("/api/v1/trade-orders")
    
    public ResponseEntity<List<OrderResponse>> listOrders() {
        return ResponseEntity.ok(tradeOrderService.getAll());
    }

    @PostMapping("/api/v1/trade-orders")
    
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest payload) {
        return ResponseEntity.ok(tradeOrderService.createOrder(payload));
    }

    @GetMapping("/api/v1/trade-orders/{id}")
    
    public ResponseEntity<OrderResponse> getOrder(@Parameter(description = "Trade Order ID") @PathVariable UUID id) {
        return ResponseEntity.ok(tradeOrderService.getById(id));
    }

    @PostMapping("/api/v1/trade-orders/{id}/cancel")
    
    public ResponseEntity<CancelOrderResponse> cancelOrder(@Parameter(description = "Trade Order ID") @PathVariable UUID id, @RequestBody CancelOrderRequest payload) {
        return ResponseEntity.ok(tradeOrderService.cancelOrder(id, payload));
    }

    @GetMapping("/api/v1/trade-orders/{id}/status")
    
    public ResponseEntity<Map<String, String>> getOrderStatus(@Parameter(description = "Trade Order ID") @PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("status", tradeOrderService.getById(id).getStatus()));
    }


    @GetMapping("/api/v1/portfolios/{portfolioId}/trade-orders")
    
    public ResponseEntity<List<OrderResponse>> listOrdersForPortfolio(@Parameter(description = "Portfolio ID") @PathVariable UUID portfolioId) {
        return ResponseEntity.ok(tradeOrderService.getOrdersForPortfolio(portfolioId));
    }
}
