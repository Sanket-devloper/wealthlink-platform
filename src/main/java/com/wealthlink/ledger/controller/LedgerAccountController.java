package com.wealthlink.ledger.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.wealthlink.ledger.dto.LedgerBalanceResponse;
import com.wealthlink.ledger.dto.JournalResponse.JournalEntryResponse;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Dev 3 - Portfolio, Trading & Ledger", description = "Core money-movement path")
@Tag(name = "Ledger APIs", description = "Maintained by: Rushikesh Mind")
@RestController
@RequiredArgsConstructor
public class LedgerAccountController {

    private final com.wealthlink.ledger.service.LedgerAccountService ledgerAccountService;
    private final com.wealthlink.ledger.service.JournalEntryService journalEntryService;

    @PostMapping("/api/v1/ledger-accounts")
    
    public ResponseEntity<Object> createLedgerAccount(@RequestBody com.wealthlink.ledger.dto.CreateLedgerAccountRequest payload) {
        return ResponseEntity.ok(ledgerAccountService.createLedgerAccount(payload));
    }

    @GetMapping("/api/v1/ledger-accounts/{ledgerAccountId}")
    
    public ResponseEntity<Object> getLedgerAccount(@Parameter(description = "Ledger Account ID") @PathVariable UUID ledgerAccountId) {
        return ResponseEntity.ok(ledgerAccountService.getById(ledgerAccountId));
    }

    @GetMapping("/api/v1/ledger-accounts/{ledgerAccountId}/balance")
    
    public ResponseEntity<LedgerBalanceResponse> getLedgerBalance(@Parameter(description = "Ledger Account ID") @PathVariable UUID ledgerAccountId) {
        return ResponseEntity.ok(ledgerAccountService.getBalance(ledgerAccountId));
    }

    @GetMapping("/api/v1/ledger-accounts/{ledgerAccountId}/entries")
    
    public ResponseEntity<List<JournalEntryResponse>> listLedgerAccountEntries(@Parameter(description = "Ledger Account ID") @PathVariable UUID ledgerAccountId) {
        return ResponseEntity.ok(journalEntryService.getEntriesByLedgerAccountId(ledgerAccountId));
    }

}
