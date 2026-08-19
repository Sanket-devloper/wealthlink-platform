package com.wealthlink.ledger.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JournalResponse {
    private UUID id;
    private String journalType;
    private LocalDate postingDate;
    private LocalDate valueDate;
    private String status;
    private List<JournalEntryResponse> entries;
    
    @Data
    @Builder
    public static class JournalEntryResponse {
        private UUID ledgerAccountId;
        private String direction;
        private BigDecimal amount;
        private String currency;
    }
}
