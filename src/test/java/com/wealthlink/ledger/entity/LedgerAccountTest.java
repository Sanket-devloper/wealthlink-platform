package com.wealthlink.ledger.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LedgerAccount entity lifecycle callbacks (@PrePersist / @PreUpdate).
 * No Spring context or database required.
 */
class LedgerAccountTest {

    @Test
    void onCreate_defaultsStatusToActive_whenNotSet() {
        LedgerAccount account = LedgerAccount.builder()
                .accountCode("CASH-001")
                .accountName("Cash Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .build();

        account.onCreate();

        assertThat(account.getStatus()).isEqualTo(LedgerAccountStatus.ACTIVE);
    }

    @Test
    void onCreate_defaultsBalanceToZero_whenNotSet() {
        LedgerAccount account = LedgerAccount.builder()
                .accountCode("CASH-002")
                .accountName("Cash Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .build();

        account.onCreate();

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void onCreate_setsTimestamps_whenNotSet() {
        LedgerAccount account = LedgerAccount.builder()
                .accountCode("CASH-003")
                .accountName("Cash Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .build();

        account.onCreate();

        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_doesNotOverrideExplicitStatus() {
        LedgerAccount account = LedgerAccount.builder()
                .accountCode("CASH-004")
                .accountName("Cash Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .status(LedgerAccountStatus.CLOSED)
                .build();

        account.onCreate();

        assertThat(account.getStatus()).isEqualTo(LedgerAccountStatus.CLOSED);
    }

    @Test
    void onUpdate_refreshesUpdatedAt_andPreservesCreatedAt() {
        LedgerAccount account = LedgerAccount.builder()
                .accountCode("CASH-005")
                .accountName("Cash Account")
                .ledgerAccountType(LedgerAccountType.CASH)
                .build();
        account.onCreate();
        var createdAt = account.getCreatedAt();

        account.onUpdate();

        assertThat(account.getCreatedAt()).isEqualTo(createdAt);
        assertThat(account.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
