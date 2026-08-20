package com.wealthlink.ledger.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.ledger.dto.CreateLedgerAccountRequest;
import com.wealthlink.ledger.dto.LedgerAccountResponse;
import com.wealthlink.ledger.dto.LedgerBalanceResponse;
import com.wealthlink.ledger.entity.JournalEntry;
import com.wealthlink.ledger.entity.JournalEntryDirection;
import com.wealthlink.ledger.entity.LedgerAccount;
import com.wealthlink.ledger.entity.LedgerAccountType;
import com.wealthlink.ledger.entity.LedgerAccountStatus;
import com.wealthlink.ledger.repository.JournalEntryRepository;
import com.wealthlink.ledger.repository.LedgerAccountRepository;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerAccountServiceImpl implements LedgerAccountService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final CurrencyRepository currencyRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LedgerAccountResponse> getAll() {
        return ledgerAccountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerAccountResponse getById(UUID id) {
        LedgerAccount ledgerAccount = ledgerAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LedgerAccount", id));
        return mapToResponse(ledgerAccount);
    }

    @Override
    @Transactional
    public LedgerAccountResponse createLedgerAccount(CreateLedgerAccountRequest request) {
        if (request.getAccountId() == null && request.getPortfolioId() == null) {
            throw new IllegalArgumentException("Either accountId or portfolioId must be provided");
        }

        LedgerAccount account = new LedgerAccount();
        account.setAccountCode(request.getAccountCode());
        account.setAccountName(request.getAccountName());
        account.setLedgerAccountType(LedgerAccountType.valueOf(request.getLedgerAccountType()));
        account.setStatus(LedgerAccountStatus.ACTIVE);
        account.setCurrency(currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId())));
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setDescription(request.getDescription());

        if (request.getAccountId() != null) {
            account.setAccount(accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId())));
        }
        if (request.getPortfolioId() != null) {
            account.setPortfolio(portfolioRepository.findById(request.getPortfolioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Portfolio", request.getPortfolioId())));
        }

        LedgerAccount savedAccount = ledgerAccountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerBalanceResponse getBalance(UUID ledgerAccountId) {
        LedgerAccount account = ledgerAccountRepository.findById(ledgerAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("LedgerAccount", ledgerAccountId));

        List<JournalEntry> entries = journalEntryRepository.findByLedgerAccountId(ledgerAccountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        for (JournalEntry entry : entries) {
            if (entry.getDirection() == JournalEntryDirection.DEBIT) {
                if (account.getLedgerAccountType() == LedgerAccountType.CASH || account.getLedgerAccountType() == LedgerAccountType.POSITION) {
                    balance = balance.add(entry.getAmount());
                } else {
                    balance = balance.subtract(entry.getAmount());
                }
            } else {
                if (account.getLedgerAccountType() == LedgerAccountType.TAX || account.getLedgerAccountType() == LedgerAccountType.FEE || account.getLedgerAccountType() == LedgerAccountType.SUSPENSE) {
                    balance = balance.add(entry.getAmount());
                } else {
                    balance = balance.subtract(entry.getAmount());
                }
            }
        }

        return LedgerBalanceResponse.builder()
                .ledgerAccountId(account.getId())
                .balance(balance)
                .currency(account.getCurrency().getIsoCode())
                .build();
    }

    private LedgerAccountResponse mapToResponse(LedgerAccount account) {
        return LedgerAccountResponse.builder()
                .id(account.getId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .ledgerAccountType(account.getLedgerAccountType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency().getIsoCode())
                .build();
    }
}
