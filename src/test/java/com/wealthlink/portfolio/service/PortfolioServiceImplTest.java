package com.wealthlink.portfolio.service;

import com.wealthlink.account.entity.Account;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.portfolio.dto.CreatePortfolioRequest;
import com.wealthlink.portfolio.dto.PortfolioResponse;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.PortfolioStatus;
import com.wealthlink.portfolio.entity.PortfolioType;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PortfolioServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock PortfolioRepository portfolioRepository;
    @Mock AccountRepository accountRepository;
    @Mock CurrencyRepository currencyRepository;

    @InjectMocks PortfolioServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd(UUID id) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode("USD");
        return c;
    }

    private Portfolio samplePortfolio(UUID id, UUID currencyId) {
        return Portfolio.builder()
                .id(id)
                .portfolioNumber("PORT-" + id)
                .portfolioType(PortfolioType.STANDARD)
                .baseCurrency(usd(currencyId))
                .status(PortfolioStatus.ACTIVE)
                .build();
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_returnsMappedResponse_whenPortfolioExists() {
        UUID id = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Portfolio portfolio = samplePortfolio(id, currencyId);
        when(portfolioRepository.findById(id)).thenReturn(Optional.of(portfolio));

        PortfolioResponse response = service.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPortfolioType()).isEqualTo("STANDARD");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getBaseCurrency()).isEqualTo("USD");
    }

    @Test
    void getById_throwsRuntimeException_whenPortfolioNotFound() {
        UUID id = UUID.randomUUID();
        when(portfolioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // -------------------------------------------------------------------------
    // createPortfolio
    // -------------------------------------------------------------------------

    @Test
    void createPortfolio_savesPortfolio_withActiveStatus() {
        UUID accountId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);
        Currency currency = usd(currencyId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));
        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        when(portfolioRepository.save(captor.capture())).thenAnswer(inv -> {
            Portfolio p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CreatePortfolioRequest req = new CreatePortfolioRequest();
        req.setAccountId(accountId);
        req.setPortfolioType("STANDARD");
        req.setBaseCurrencyId(currencyId);

        PortfolioResponse response = service.createPortfolio(req);

        Portfolio saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(PortfolioStatus.ACTIVE);
        assertThat(saved.getPortfolioType()).isEqualTo(PortfolioType.STANDARD);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void createPortfolio_throws_whenAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        CreatePortfolioRequest req = new CreatePortfolioRequest();
        req.setAccountId(accountId);
        req.setPortfolioType("DISCRETIONARY");
        req.setBaseCurrencyId(UUID.randomUUID());

        assertThatThrownBy(() -> service.createPortfolio(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // -------------------------------------------------------------------------
    // getValuation
    // -------------------------------------------------------------------------

    @Test
    void getValuation_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> service.getValuation(UUID.randomUUID(), LocalDate.now()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
