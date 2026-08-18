package com.wealthlink.portfolio;

import com.wealthlink.account.entity.Account;
import com.wealthlink.account.entity.AccountType;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.fund.entity.Fund;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.PortfolioType;
import com.wealthlink.portfolio.entity.PortfolioValuationSnapshot;
import com.wealthlink.portfolio.entity.Position;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.portfolio.repository.PortfolioValuationSnapshotRepository;
import com.wealthlink.portfolio.repository.PositionRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioRepositoryIT extends AbstractIntegrationTest {

    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private PortfolioValuationSnapshotRepository valuationRepository;
    @Autowired private PositionRepository positionRepository;
    
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private FundRepository fundRepository;
    @Autowired private FundShareClassRepository fundShareClassRepository;

    private Currency nok;
    private Account account;
    private FundShareClass shareClass;

    @BeforeEach
    void setUp() {
        nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        account = accountRepository.saveAndFlush(Account.builder()
                .accountNumber("ACC-PORT-" + UUID.randomUUID().toString().substring(0, 8))
                .accountType(AccountType.INVESTMENT)
                .currency(nok)
                .country(norway)
                .build());

        Fund fund = fundRepository.saveAndFlush(Fund.builder()
                .isin("NO001000000" + System.currentTimeMillis() % 10)
                .name("Test Fund")
                .baseCurrency(nok)
                .domicileCountry(norway)
                .build());

        shareClass = fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fund)
                .classCode("A")
                .currency(nok)
                .build());
    }

    @Test
    void canSaveAndRetrievePortfolio() {
        Portfolio p = Portfolio.builder()
                .account(account)
                .portfolioNumber("PORT-" + UUID.randomUUID())
                .portfolioType(PortfolioType.STANDARD)
                .baseCurrency(nok)
                .build();
        
        portfolioRepository.saveAndFlush(p);

        assertThat(p.getId()).isNotNull();
        assertThat(portfolioRepository.findById(p.getId())).isPresent();
    }

    @Test
    void portfolioNumberMustBeUnique() {
        String num = "DUPE-PORT";
        Portfolio p1 = Portfolio.builder().account(account).portfolioNumber(num).portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build();
        portfolioRepository.saveAndFlush(p1);

        Portfolio p2 = Portfolio.builder().account(account).portfolioNumber(num).portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build();
        assertThatThrownBy(() -> portfolioRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void canSaveValuationSnapshot() {
        Portfolio p = portfolioRepository.saveAndFlush(Portfolio.builder().account(account).portfolioNumber("V-PORT").portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build());
        
        PortfolioValuationSnapshot snap = PortfolioValuationSnapshot.builder()
                .portfolio(p)
                .valuationDate(LocalDate.now())
                .totalValue(new BigDecimal("10000.500000"))
                .currency(nok)
                .build();
                
        valuationRepository.saveAndFlush(snap);
        assertThat(snap.getId()).isNotNull();
    }
    
    @Test
    void valuationSnapshotUniquePerPortfolioAndDate() {
        Portfolio p = portfolioRepository.saveAndFlush(Portfolio.builder().account(account).portfolioNumber("V2-PORT").portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build());
        LocalDate dt = LocalDate.now();
        
        PortfolioValuationSnapshot snap1 = PortfolioValuationSnapshot.builder().portfolio(p).valuationDate(dt).totalValue(BigDecimal.TEN).currency(nok).build();
        valuationRepository.saveAndFlush(snap1);
        
        PortfolioValuationSnapshot snap2 = PortfolioValuationSnapshot.builder().portfolio(p).valuationDate(dt).totalValue(BigDecimal.ONE).currency(nok).build();
        assertThatThrownBy(() -> valuationRepository.saveAndFlush(snap2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void canSavePosition() {
        Portfolio p = portfolioRepository.saveAndFlush(Portfolio.builder().account(account).portfolioNumber("POS-PORT").portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build());
        
        Position pos = Position.builder()
                .portfolio(p)
                .fundShareClass(shareClass)
                .positionDate(LocalDate.now())
                .quantity(new BigDecimal("150.12345678"))
                .averageCost(new BigDecimal("100.00000000"))
                .costBasisCurrency(nok)
                .marketValue(new BigDecimal("15018.518518"))
                .currency(nok)
                .build();
                
        positionRepository.saveAndFlush(pos);
        assertThat(pos.getId()).isNotNull();
    }
    
    @Test
    void positionQuantityMustBePositive() {
        Portfolio p = portfolioRepository.saveAndFlush(Portfolio.builder().account(account).portfolioNumber("POS2-PORT").portfolioType(PortfolioType.STANDARD).baseCurrency(nok).build());
        
        Position pos = Position.builder()
                .portfolio(p)
                .fundShareClass(shareClass)
                .positionDate(LocalDate.now())
                .quantity(new BigDecimal("-10.00000000"))
                .averageCost(BigDecimal.ZERO)
                .costBasisCurrency(nok)
                .marketValue(BigDecimal.ZERO)
                .currency(nok)
                .build();
                
        assertThatThrownBy(() -> positionRepository.saveAndFlush(pos))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
