package com.wealthlink.ledger.service;

import com.wealthlink.ledger.dto.CreateLedgerAccountRequest;
import com.wealthlink.ledger.dto.LedgerAccountResponse;
import com.wealthlink.ledger.dto.LedgerBalanceResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for LedgerAccount (chart-of-accounts) management.
 */
public interface LedgerAccountService {

    /** Retrieve all ledger accounts. */
    List<LedgerAccountResponse> getAll();

    /** Retrieve a single ledger account by its primary key. */
    LedgerAccountResponse getById(UUID id);

    /**
     * Create a new ledger account linked to either a client Account or a Portfolio.
     * At least one of {@code request.accountId} or {@code request.portfolioId} must be provided.
     */
    LedgerAccountResponse createLedgerAccount(CreateLedgerAccountRequest request);

    /**
     * Compute and return the current running balance for a ledger account
     * by replaying all its journal entries.
     */
    LedgerBalanceResponse getBalance(UUID ledgerAccountId);
}
