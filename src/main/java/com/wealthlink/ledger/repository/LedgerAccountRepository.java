package com.wealthlink.ledger.repository;

import com.wealthlink.ledger.entity.LedgerAccount;
import com.wealthlink.ledger.entity.LedgerAccountStatus;
import com.wealthlink.ledger.entity.LedgerAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {
    Optional<LedgerAccount> findByAccountCode(String accountCode);
    List<LedgerAccount> findByLedgerAccountType(LedgerAccountType accountType);
    List<LedgerAccount> findByStatus(LedgerAccountStatus status);
    Optional<LedgerAccount> findByAccountIdAndLedgerAccountTypeAndCurrencyId(UUID accountId, LedgerAccountType type, UUID currencyId);
    Optional<LedgerAccount> findByPortfolioIdAndLedgerAccountTypeAndCurrencyId(UUID portfolioId, LedgerAccountType type, UUID currencyId);
}
