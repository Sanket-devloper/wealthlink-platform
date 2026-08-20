package com.wealthlink.ledger.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateJournalEntryRequest {
    private UUID ledgerAccountId;
    private String direction;
    private BigDecimal amount;
    private UUID currencyId;
    private String description;
}
