package com.wealthlink.reconciliation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReconciliationStateException extends RuntimeException {
    public InvalidReconciliationStateException(String message) {
        super(message);
    }
}