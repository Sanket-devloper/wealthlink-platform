package com.wealthlink.account;

import com.wealthlink.account.entity.Account;
import com.wealthlink.account.entity.AccountOwner;
import com.wealthlink.account.entity.AccountStatus;
import com.wealthlink.account.entity.AccountType;
import com.wealthlink.account.entity.OwnershipRole;
import com.wealthlink.account.repository.AccountOwnerRepository;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.customer.entity.Customer;
import com.wealthlink.customer.entity.CustomerType;
import com.wealthlink.customer.repository.CustomerRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer tests for Accounts: ACCOUNT, ACCOUNT_OWNER.
 */
class AccountRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountOwnerRepository accountOwnerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Account newAccount(String accountNumber) {
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        return Account.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.INVESTMENT)
                .currency(nok)
                .country(norway)
                .build();
    }

    @Test
    void savingAnAccountDefaultsStatusAndOpenedAt() {
        Account saved = accountRepository.saveAndFlush(newAccount("ACC-0001"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE); // @PrePersist default
        assertThat(saved.getOpenedAt()).isNotNull();
    }

    @Test
    void accountNumberUniqueConstraintIsEnforced() {
        accountRepository.saveAndFlush(newAccount("ACC-0002"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(newAccount("ACC-0002")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void jointAccountCanHaveTwoOwners() {
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        Account account = accountRepository.saveAndFlush(newAccount("ACC-0003"));

        Customer customerA = customerRepository.saveAndFlush(Customer.builder()
                .customerNumber("CUST-JOINT-A")
                .customerType(CustomerType.INDIVIDUAL)
                .country(norway)
                .taxResidencyCountry(norway)
                .build());

        Customer customerB = customerRepository.saveAndFlush(Customer.builder()
                .customerNumber("CUST-JOINT-B")
                .customerType(CustomerType.INDIVIDUAL)
                .country(norway)
                .taxResidencyCountry(norway)
                .build());

        accountOwnerRepository.saveAndFlush(AccountOwner.builder()
                .account(account)
                .customer(customerA)
                .ownershipRole(OwnershipRole.PRIMARY)
                .build());

        accountOwnerRepository.saveAndFlush(AccountOwner.builder()
                .account(account)
                .customer(customerB)
                .ownershipRole(OwnershipRole.JOINT)
                .build());

        assertThat(accountOwnerRepository.findByAccountId(account.getId())).hasSize(2);
    }
}
