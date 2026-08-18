package com.wealthlink.dividend;

import com.wealthlink.account.entity.Account;
import com.wealthlink.account.entity.AccountType;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.dividend.entity.DividendAllocation;
import com.wealthlink.dividend.entity.DividendAllocationStatus;
import com.wealthlink.dividend.entity.DividendEvent;
import com.wealthlink.dividend.repository.DividendAllocationRepository;
import com.wealthlink.dividend.repository.DividendEventRepository;
import com.wealthlink.fund.entity.Fund;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.PortfolioType;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DividendRepositoryIT {

    @Autowired private DividendEventRepository eventRepository;
    @Autowired private DividendAllocationRepository allocationRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private FundRepository fundRepository;
    @Autowired private FundShareClassRepository fundShareClassRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PortfolioRepository portfolioRepository;

    private Currency nok;
    private FundShareClass shareClass;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        Account account = accountRepository.saveAndFlush(Account.builder()
                .accountNumber("ACC-DIV-" + UUID.randomUUID().toString().substring(0, 8))
                .accountType(AccountType.INVESTMENT)
                .currency(nok)
                .country(norway)
                .build());

        portfolio = portfolioRepository.saveAndFlush(Portfolio.builder()
                .account(account)
                .portfolioNumber("PORT-DIV-" + UUID.randomUUID().toString().substring(0, 8))
                .portfolioType(PortfolioType.STANDARD)
                .baseCurrency(nok)
                .build());

        Fund fund = fundRepository.saveAndFlush(Fund.builder()
                .isin("NO00" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                .name("Dividend Test Fund")
                .baseCurrency(nok)
                .domicileCountry(norway)
                .build());

        shareClass = fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fund)
                .classCode("DIV-" + UUID.randomUUID().toString().substring(0, 4))
                .name("Class A")
                .currency(nok)
                .build());
    }

    @Test
    void canPersistDividendEventAndAllocation() {
        DividendEvent event = eventRepository.saveAndFlush(DividendEvent.builder()
                .fundShareClass(shareClass)
                .currency(nok)
                .exDate(LocalDate.now())
                .recordDate(LocalDate.now())
                .paymentDate(LocalDate.now().plusDays(2))
                .dividendPerUnit(new BigDecimal("2.50000000"))
                .source("MANUAL")
                .build());

        DividendAllocation allocation = allocationRepository.saveAndFlush(DividendAllocation.builder()
                .dividendEvent(event)
                .portfolio(portfolio)
                .positionQuantity(new BigDecimal("100.00000000"))
                .grossAmount(new BigDecimal("250.000000"))
                .taxAmount(new BigDecimal("50.000000"))
                .netAmount(new BigDecimal("200.000000"))
                .currency(nok)
                .build());

        assertThat(allocation.getId()).isNotNull();
        assertThat(allocation.getStatus()).isEqualTo(DividendAllocationStatus.PENDING);
    }

    @Test
    void duplicateAllocationForSameEventAndPortfolioIsRejected() {
        DividendEvent event = eventRepository.saveAndFlush(DividendEvent.builder()
                .fundShareClass(shareClass)
                .currency(nok)
                .exDate(LocalDate.now())
                .recordDate(LocalDate.now())
                .paymentDate(LocalDate.now().plusDays(2))
                .dividendPerUnit(new BigDecimal("1.00000000"))
                .source("MANUAL")
                .build());

        DividendAllocation alloc1 = DividendAllocation.builder()
                .dividendEvent(event)
                .portfolio(portfolio)
                .positionQuantity(new BigDecimal("10.00000000"))
                .grossAmount(new BigDecimal("10.000000"))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(new BigDecimal("10.000000"))
                .currency(nok)
                .build();
        allocationRepository.saveAndFlush(alloc1);

        DividendAllocation alloc2 = DividendAllocation.builder()
                .dividendEvent(event)
                .portfolio(portfolio)
                .positionQuantity(new BigDecimal("10.00000000"))
                .grossAmount(new BigDecimal("10.000000"))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(new BigDecimal("10.000000"))
                .currency(nok)
                .build();

        assertThatThrownBy(() -> allocationRepository.saveAndFlush(alloc2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}