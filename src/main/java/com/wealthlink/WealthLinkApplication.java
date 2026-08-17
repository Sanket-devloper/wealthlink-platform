package com.wealthlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

/**
 * <p>
 * Modular monolith - this codebase currently contains the modules owned by
 * Dev 1 ("Foundation"): Identity & Access, Country / Market Config (Reference Data),
 * Customer Management, and Accounts. These are the table everyone else's
 * modules (Funds/FX, Portfolio/Trading/Ledger, Dividends/Reconciliation/Audit)
 * foreign-key into, per the architecture doc Section 15 ("Dev 1 is the true
 * dependency root").
 */
@SpringBootApplication
public class WealthLinkApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(WealthLinkApplication.class, args);
    }
}
