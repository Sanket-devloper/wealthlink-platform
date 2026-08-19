package com.wealthlink.portfolio.controller;

import com.wealthlink.portfolio.service.PortfolioValuationSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfoliovaluationsnapshots")
@RequiredArgsConstructor
public class PortfolioValuationSnapshotController {

    private final PortfolioValuationSnapshotService service;

    @GetMapping
    
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
