package com.wealthlink.ledger.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.wealthlink.ledger.dto.JournalResponse;
import com.wealthlink.ledger.dto.JournalResponse.JournalEntryResponse;
import com.wealthlink.ledger.dto.ReverseJournalRequest;
import com.wealthlink.ledger.dto.ReverseJournalResponse;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Ledger APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class JournalController {

    private final com.wealthlink.ledger.service.JournalService journalService;
    private final com.wealthlink.ledger.service.JournalEntryService journalEntryService;

    @PostMapping("/api/v1/journals")
    
    public ResponseEntity<JournalResponse> createJournal(@RequestBody com.wealthlink.ledger.dto.CreateJournalRequest payload) {
        return ResponseEntity.ok(journalService.createJournal(payload));
    }

    @GetMapping("/api/v1/journals/{journalId}")
    
    public ResponseEntity<JournalResponse> getJournal(@Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        return ResponseEntity.ok(journalService.getById(journalId));
    }

    @GetMapping("/api/v1/journals/{journalId}/entries")
    
    public ResponseEntity<List<JournalEntryResponse>> listJournalEntries(@Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        return ResponseEntity.ok(journalEntryService.getEntriesByJournalId(journalId));
    }

    // Keep reverse journal as it's useful, though not strictly in the basic requirements
    @PostMapping("/api/v1/journals/{id}/reverse")
    
    public ResponseEntity<ReverseJournalResponse> reverseJournal(@Parameter(description = "Journal ID") @PathVariable UUID id, @RequestBody ReverseJournalRequest payload) {
        return ResponseEntity.ok(journalService.reverseJournal(id, payload));
    }

}
