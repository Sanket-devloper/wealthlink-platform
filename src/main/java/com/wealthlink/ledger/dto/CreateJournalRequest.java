package com.wealthlink.ledger.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateJournalRequest {
    private String description;
    private String journalType;
    private LocalDate journalDate;
    private String referenceType;
    private UUID referenceId;
    private String idempotencyKey;
    private List<CreateJournalEntryRequest> entries;
}
