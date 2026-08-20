package com.wealthlink.fund.exception;

import java.util.UUID;

public class CountryNotFoundException extends RuntimeException {

    public CountryNotFoundException(UUID countryId) {
        super("Country not found with id: " + countryId);
    }
}