package com.wealthlink.marketdata.exception;

public class FxRateNotFoundException extends RuntimeException {

    public FxRateNotFoundException(String message) {
        super(message);
    }
}