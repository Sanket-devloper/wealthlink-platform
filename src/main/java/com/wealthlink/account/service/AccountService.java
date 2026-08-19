package com.wealthlink.account.service;

import com.wealthlink.account.dto.*;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountResponse> findAll();
    AccountResponse findById(UUID id);
    AccountResponse create(AccountRequest request);
    AccountResponse update(UUID id, AccountRequest request);
    void delete(UUID id);
    List<AccountOwnerResponse> findOwners(UUID accountId);
    AccountOwnerResponse addOwner(UUID accountId, AccountOwnerRequest request);
}