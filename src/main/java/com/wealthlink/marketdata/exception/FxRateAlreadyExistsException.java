package com.wealthlink.marketdata.exception;

public class FxRateAlreadyExistsException extends RuntimeException {

    public FxRateAlreadyExistsException(String message) {
        super(message);
    }
}