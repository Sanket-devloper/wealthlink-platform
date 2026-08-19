package com.wealthlink.ledger.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateLedgerAccountRequest {
    private String accountCode;
    private String accountName;
    private String ledgerAccountType;
    private UUID currencyId;
    private UUID accountId;
    private UUID portfolioId;
    private String description;
}
