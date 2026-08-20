package com.wealthlink.fund.exception;

import java.util.UUID;

public class FundShareClassNotFoundException extends RuntimeException {

    public FundShareClassNotFoundException(UUID id) {
        super("Fund share class not found with id: " + id);
    }
}