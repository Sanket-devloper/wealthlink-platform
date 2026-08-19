package com.wealthlink.account.service.impl;

import com.wealthlink.account.service.AccountService;

import com.wealthlink.account.dto.*;
import com.wealthlink.account.entity.Account;
import com.wealthlink.account.entity.AccountOwner;
import com.wealthlink.account.repository.AccountOwnerRepository;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.customer.entity.Customer;
import com.wealthlink.customer.repository.CustomerRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountOwnerRepository accountOwnerRepository;
    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AccountResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        Currency currency = findCurrency(request.currencyId());
        Country country = findCountry(request.countryId());

        Account account = Account.builder()
                .accountNumber(request.accountNumber())
                .accountType(request.accountType())
                .currency(currency)
                .country(country)
                .status(request.status())
                .build();
        return toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse update(UUID id, AccountRequest request) {
        Account account = getOrThrow(id);
        account.setAccountNumber(request.accountNumber());
        account.setAccountType(request.accountType());
        account.setCurrency(findCurrency(request.currencyId()));
        account.setCountry(findCountry(request.countryId()));
        if (request.status() != null) {
            account.setStatus(request.status());
        }
        return toResponse(accountRepository.save(account));
    }

    @Override
    public void delete(UUID id) {
        accountRepository.delete(getOrThrow(id));
    }

    @Override
    public List<AccountOwnerResponse> findOwners(UUID accountId) {
        return accountOwnerRepository.findByAccountId(accountId).stream()
                .map(this::toOwnerResponse).toList();
    }

    @Override
    public AccountOwnerResponse addOwner(UUID accountId, AccountOwnerRequest request) {
        Account account = getOrThrow(accountId);
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        AccountOwner owner = AccountOwner.builder()
                .account(account)
                .customer(customer)
                .ownershipRole(request.ownershipRole())
                .build();
        return toOwnerResponse(accountOwnerRepository.save(owner));
    }

    private Account getOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    private Currency findCurrency(UUID id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    private Country findCountry(UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getId(), a.getAccountNumber(), a.getAccountType(),
                a.getCurrency().getId(), a.getCurrency().getIsoCode(),
                a.getCountry().getId(), a.getCountry().getIsoCode(),
                a.getStatus(), a.getOpenedAt());
    }

    private AccountOwnerResponse toOwnerResponse(AccountOwner ao) {
        return new AccountOwnerResponse(ao.getId(), ao.getAccount().getId(),
                ao.getCustomer().getId(), ao.getCustomer().getCustomerNumber(), ao.getOwnershipRole());
    }
}
