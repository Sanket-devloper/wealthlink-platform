package com.wealthlink.account.repository;

import com.wealthlink.account.entity.AccountOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountOwnerRepository extends JpaRepository<AccountOwner, UUID> {
    List<AccountOwner> findByAccountId(UUID accountId);
    List<AccountOwner> findByCustomerId(UUID customerId);
}
