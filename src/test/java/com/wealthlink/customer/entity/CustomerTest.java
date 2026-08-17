package com.wealthlink.customer.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-level unit test (Day 1 deliverable) - no database, verifies the
 * @PrePersist defaulting logic directly on the entity.
 */
class CustomerTest {

    @Test
    void onCreateDefaultsStatusToPendingKycWhenNotSet() {
        Customer customer = Customer.builder()
                .customerNumber("CUST-TEST-1")
                .customerType(CustomerType.INDIVIDUAL)
                .build();

        customer.onCreate();

        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.PENDING_KYC);
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreateDoesNotOverrideAnExplicitStatus() {
        Customer customer = Customer.builder()
                .customerNumber("CUST-TEST-2")
                .customerType(CustomerType.CORPORATE)
                .status(CustomerStatus.ACTIVE)
                .build();

        customer.onCreate();

        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }
}
