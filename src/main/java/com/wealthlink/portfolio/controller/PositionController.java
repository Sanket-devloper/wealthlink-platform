package com.wealthlink.portfolio.controller;

import com.wealthlink.portfolio.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ResponseEntity<List<com.wealthlink.portfolio.dto.PositionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ResponseEntity<com.wealthlink.portfolio.dto.PositionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/rebuild")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<com.wealthlink.portfolio.dto.PositionResponse> rebuildPosition(@PathVariable UUID id) {
        return ResponseEntity.ok(service.rebuildPosition(id));
    }
}
