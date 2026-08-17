package com.wealthlink.account.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-level unit test (Day 1 deliverable) - no database, verifies the
 * @PrePersist defaulting logic directly on the entity.
 */
class AccountTest {

    @Test
    void onCreateDefaultsStatusToActiveAndSetsOpenedAtWhenNotSet() {
        Account account = Account.builder()
                .accountNumber("ACC-TEST-1")
                .accountType(AccountType.CASH)
                .build();

        account.onCreate();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getOpenedAt()).isNotNull();
    }

    @Test
    void onCreateDoesNotOverrideAnExplicitStatusOrOpenedAt() {
        var explicitOpenedAt = java.time.Instant.parse("2024-01-01T00:00:00Z");

        Account account = Account.builder()
                .accountNumber("ACC-TEST-2")
                .accountType(AccountType.INVESTMENT)
                .status(AccountStatus.DORMANT)
                .openedAt(explicitOpenedAt)
                .build();

        account.onCreate();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.DORMANT);
        assertThat(account.getOpenedAt()).isEqualTo(explicitOpenedAt);
    }
}
