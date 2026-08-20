package com.wealthlink.fund.exception;

public class DuplicateFundException extends RuntimeException {

    public DuplicateFundException(String isin) {
        super("Fund already exists with ISIN: " + isin);
    }
}
