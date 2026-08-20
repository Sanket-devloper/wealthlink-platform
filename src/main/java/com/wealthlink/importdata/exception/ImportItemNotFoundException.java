package com.wealthlink.importdata.exception;

import java.util.UUID;

public class ImportItemNotFoundException extends RuntimeException {

    public ImportItemNotFoundException(UUID id) {
        super("Import item not found with id: " + id);
    }
}