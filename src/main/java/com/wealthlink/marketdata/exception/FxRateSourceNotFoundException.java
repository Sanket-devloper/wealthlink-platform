package com.wealthlink.marketdata.exception;

public class FxRateSourceNotFoundException extends RuntimeException {

    public FxRateSourceNotFoundException(String message) {
        super(message);
    }
}