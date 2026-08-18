package com.wealthlink.reconciliation.entity;

public enum ReconciliationMatchStatus {
    MATCHED,
    MISSING_INTERNAL,
    MISSING_EXTERNAL,
    AMOUNT_MISMATCH,
    QUANTITY_MISMATCH,
    PRICE_MISMATCH,
    DATE_MISMATCH,
    DUPLICATE,
    ERROR
}