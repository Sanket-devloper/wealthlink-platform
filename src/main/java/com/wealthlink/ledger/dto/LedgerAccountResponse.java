package com.wealthlink.ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class LedgerAccountResponse {
    private UUID id;
    private String accountCode;
    private String accountName;
    private String ledgerAccountType;
    private String status;
    private String currency;
    private BigDecimal balance;
}
