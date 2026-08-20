package com.wealthlink.marketdata.exception;

public class FundPriceAlreadyExistsException extends RuntimeException {

    public FundPriceAlreadyExistsException(String message) {
        super(message);
    }
}