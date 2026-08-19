package com.wealthlink.fund.exception;

import java.util.UUID;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(UUID currencyId) {
        super("Currency not found with id: " + currencyId);
    }
}