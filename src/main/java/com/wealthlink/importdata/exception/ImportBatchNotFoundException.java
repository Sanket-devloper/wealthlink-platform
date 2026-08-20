package com.wealthlink.importdata.exception;

import java.util.UUID;

public class ImportBatchNotFoundException extends RuntimeException {

    public ImportBatchNotFoundException(UUID id) {
        super("Import batch not found with id: " + id);
    }
}