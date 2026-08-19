package com.wealthlink.ledger.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReverseJournalResponse {
    private UUID reversalJournalId;
    private UUID reversedJournalId;
    private String journalType;
    private String status;
    private LocalDate postingDate;
}
