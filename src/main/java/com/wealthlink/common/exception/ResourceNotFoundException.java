package com.wealthlink.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entityName, UUID id) {
        super(entityName + " not found with id: " + id);
    }
}
