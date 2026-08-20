package com.wealthlink.portfolio.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

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
import com.wealthlink.portfolio.entity.PortfolioStatus;

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
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAll() {
        return portfolioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getById(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));
        return mapToResponse(portfolio);
    }

    @Override
    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setAccount(accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId())));
        portfolio.setPortfolioNumber(UUID.randomUUID().toString()); // Stub for generation
        portfolio.setPortfolioType(PortfolioType.valueOf(request.getPortfolioType()));
        portfolio.setBaseCurrency(currencyRepository.findById(request.getBaseCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getBaseCurrencyId())));
        portfolio.setStatus(PortfolioStatus.ACTIVE);

        Portfolio saved = portfolioRepository.save(portfolio);
        return mapToResponse(saved);
    }

    @Override
    public ValuationResponse getValuation(UUID portfolioId, LocalDate valuationDate) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public PortfolioResponse updatePortfolio(UUID id, com.wealthlink.portfolio.dto.UpdatePortfolioRequest request) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));
        if (request.getStatus() != null) {
            portfolio.setStatus(PortfolioStatus.valueOf(request.getStatus()));
        }
        return mapToResponse(portfolioRepository.save(portfolio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPortfoliosByAccountId(UUID accountId) {
        return portfolioRepository.findByAccountId(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .portfolioNumber(portfolio.getPortfolioNumber())
                .portfolioType(portfolio.getPortfolioType().name())
                .baseCurrency(portfolio.getBaseCurrency().getIsoCode())
                .status(portfolio.getStatus().name())
                .build();
    }
}
