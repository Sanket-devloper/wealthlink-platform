package com.wealthlink.ledger.service;

import com.wealthlink.account.entity.Account;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.ledger.dto.CreateLedgerAccountRequest;
import com.wealthlink.ledger.dto.LedgerAccountResponse;
import com.wealthlink.ledger.dto.LedgerBalanceResponse;
import com.wealthlink.ledger.entity.*;
import com.wealthlink.ledger.repository.JournalEntryRepository;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LedgerAccountServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class LedgerAccountServiceImplTest {

    @Mock LedgerAccountRepository ledgerAccountRepository;
    @Mock AccountRepository accountRepository;
    @Mock PortfolioRepository portfolioRepository;
    @Mock CurrencyRepository currencyRepository;
    @Mock JournalEntryRepository journalEntryRepository;

    @InjectMocks LedgerAccountServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd(UUID id) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode("USD");
        return c;
    }

    private LedgerAccount cashAccount(UUID id, UUID currencyId) {
        return LedgerAccount.builder()
                .id(id)
                .accountCode("CASH-001")
                .accountName("Cash Account USD")
                .ledgerAccountType(LedgerAccountType.CASH)
                .status(LedgerAccountStatus.ACTIVE)
                .currency(usd(currencyId))
                .balance(BigDecimal.ZERO)
                .build();
    }

    private JournalEntry entry(JournalEntryDirection direction, BigDecimal amount, Currency currency) {
        return JournalEntry.builder()
                .id(UUID.randomUUID())
                .direction(direction)
                .amount(amount)
                .currency(currency)
                .build();
    }

    // -------------------------------------------------------------------------
    // createLedgerAccount — validation
    // -------------------------------------------------------------------------

    @Test
    void createLedgerAccount_throws_whenNeitherAccountIdNorPortfolioIdProvided() {
        CreateLedgerAccountRequest req = new CreateLedgerAccountRequest();
        req.setAccountCode("CASH-001");
        req.setAccountName("Cash");
        req.setLedgerAccountType("CASH");
        req.setCurrencyId(UUID.randomUUID());
        // accountId and portfolioId both null

        assertThatThrownBy(() -> service.createLedgerAccount(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accountId or portfolioId");
    }

    // -------------------------------------------------------------------------
    // createLedgerAccount — happy paths
    // -------------------------------------------------------------------------

    @Test
    void createLedgerAccount_createsAccount_linkedToAccount() {
        UUID currencyId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Currency currency = usd(currencyId);
        Account account = new Account();
        account.setId(accountId);

        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(ledgerAccountRepository.save(any(LedgerAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateLedgerAccountRequest req = new CreateLedgerAccountRequest();
        req.setAccountCode("CASH-002");
        req.setAccountName("Cash Account");
        req.setLedgerAccountType("CASH");
        req.setCurrencyId(currencyId);
        req.setAccountId(accountId);

        LedgerAccountResponse response = service.createLedgerAccount(req);

        assertThat(response.getAccountCode()).isEqualTo("CASH-002");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getLedgerAccountType()).isEqualTo("CASH");
    }

    @Test
    void createLedgerAccount_createsAccount_linkedToPortfolio() {
        UUID currencyId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();
        Currency currency = usd(currencyId);
        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(ledgerAccountRepository.save(any(LedgerAccount.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateLedgerAccountRequest req = new CreateLedgerAccountRequest();
        req.setAccountCode("POS-001");
        req.setAccountName("Position Account");
        req.setLedgerAccountType("POSITION");
        req.setCurrencyId(currencyId);
        req.setPortfolioId(portfolioId);

        LedgerAccountResponse response = service.createLedgerAccount(req);

        assertThat(response.getAccountCode()).isEqualTo("POS-001");
        assertThat(response.getLedgerAccountType()).isEqualTo("POSITION");
    }

    // -------------------------------------------------------------------------
    // getBalance
    // -------------------------------------------------------------------------

    @Test
    void getBalance_returnsPositiveBalance_forCashAccount_withDebitEntry() {
        UUID accountId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Currency currency = usd(currencyId);
        LedgerAccount account = cashAccount(accountId, currencyId);

        JournalEntry debit = entry(JournalEntryDirection.DEBIT, BigDecimal.valueOf(1000), currency);
        JournalEntry credit = entry(JournalEntryDirection.CREDIT, BigDecimal.valueOf(300), currency);

        when(ledgerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(journalEntryRepository.findByLedgerAccountId(accountId))
                .thenReturn(Arrays.asList(debit, credit));

        LedgerBalanceResponse response = service.getBalance(accountId);

        // CASH: DEBIT adds (+1000), CREDIT subtracts (-300) => 700
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getLedgerAccountId()).isEqualTo(accountId);
    }

    @Test
    void getBalance_returnsPositiveBalance_forFeeAccount_withCreditEntry() {
        UUID accountId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Currency currency = usd(currencyId);

        // FEE account: CREDIT adds, DEBIT subtracts
        LedgerAccount feeAccount = LedgerAccount.builder()
                .id(accountId)
                .accountCode("FEE-001")
                .accountName("Fee Account")
                .ledgerAccountType(LedgerAccountType.FEE)
                .status(LedgerAccountStatus.ACTIVE)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build();

        JournalEntry credit = entry(JournalEntryDirection.CREDIT, BigDecimal.valueOf(50), currency);

        when(ledgerAccountRepository.findById(accountId)).thenReturn(Optional.of(feeAccount));
        when(journalEntryRepository.findByLedgerAccountId(accountId))
                .thenReturn(List.of(credit));

        LedgerBalanceResponse response = service.getBalance(accountId);

        // FEE: CREDIT adds => 50
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void getBalance_returnsZero_whenNoEntries() {
        UUID accountId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        LedgerAccount account = cashAccount(accountId, currencyId);

        when(ledgerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(journalEntryRepository.findByLedgerAccountId(accountId)).thenReturn(List.of());

        LedgerBalanceResponse response = service.getBalance(accountId);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getBalance_throwsResourceNotFoundException_whenLedgerAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        when(ledgerAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance(accountId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
