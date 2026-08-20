package com.wealthlink.importdata.exception;

import java.util.UUID;

public class ImportJobNotFoundException extends RuntimeException {

    public ImportJobNotFoundException(UUID id) {
        super("Import job not found with id: " + id);
    }
}