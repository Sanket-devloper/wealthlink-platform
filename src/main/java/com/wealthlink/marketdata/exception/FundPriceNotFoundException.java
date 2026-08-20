package com.wealthlink.marketdata.exception;

public class FundPriceNotFoundException extends RuntimeException {

    public FundPriceNotFoundException(String message) {
        super(message);
    }
}