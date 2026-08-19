package com.wealthlink.ledger.dto;

import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class CreateLedgerAccountRequest {
    private String accountCode;
    private String accountName;
    private String ledgerAccountType;
    private UUID currencyId;
    private UUID accountId;
    private UUID portfolioId;
    private BigDecimal balance;
    private String description;
}
