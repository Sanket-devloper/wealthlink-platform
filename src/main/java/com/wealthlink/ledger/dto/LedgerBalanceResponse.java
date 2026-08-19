package com.wealthlink.ledger.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class LedgerBalanceResponse {
    private UUID ledgerAccountId;
    private String currency;
    private BigDecimal balance;
    private LocalDate asOf;
}
