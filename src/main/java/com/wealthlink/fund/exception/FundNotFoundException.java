package com.wealthlink.fund.exception;

import java.util.UUID;

public class FundNotFoundException extends RuntimeException {

    public FundNotFoundException(UUID fundId) {
        super("Fund not found with id: " + fundId);
    }
}