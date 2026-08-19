package com.wealthlink.portfolio.service;

import com.wealthlink.portfolio.dto.CreatePortfolioRequest;
import com.wealthlink.portfolio.dto.PortfolioResponse;
import com.wealthlink.portfolio.dto.ValuationResponse;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.PortfolioType;
import com.wealthlink.account.repository.AccountRepository;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    public List<PortfolioResponse> getAll() {
        return portfolioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioResponse getById(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        return mapToResponse(portfolio);
    }

    @Override
    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setAccount(accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found")));
        portfolio.setPortfolioNumber(UUID.randomUUID().toString()); // Stub for generation
        portfolio.setPortfolioType(PortfolioType.valueOf(request.getPortfolioType()));
        portfolio.setBaseCurrency(currencyRepository.findById(request.getBaseCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found")));
        portfolio.setStatus("ACTIVE");

        Portfolio saved = portfolioRepository.save(portfolio);
        return mapToResponse(saved);
    }

    @Override
    public ValuationResponse getValuation(UUID portfolioId, LocalDate valuationDate) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .accountId(portfolio.getAccount().getId())
                .portfolioNumber(portfolio.getPortfolioNumber())
                .portfolioType(portfolio.getPortfolioType().name())
                .baseCurrency(portfolio.getBaseCurrency().getIsoCode())
                .status(portfolio.getStatus())
                .build();
    }
}
