package com.wealthlink.ledger.controller;

import com.wealthlink.ledger.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/journalentrys")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService service;

    @GetMapping
    
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
