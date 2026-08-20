package com.wealthlink.portfolio.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.portfolio.dto.ValuationResponse;
import com.wealthlink.portfolio.entity.PortfolioValuationSnapshot;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.portfolio.repository.PortfolioValuationSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioValuationSnapshotServiceImpl implements PortfolioValuationSnapshotService {

    private final PortfolioValuationSnapshotRepository snapshotRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    public List<ValuationResponse> getAll() {
        return snapshotRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ValuationResponse getById(UUID id) {
        PortfolioValuationSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioValuationSnapshot", id));
        return mapToResponse(snapshot);
    }

    @Override
    @Transactional
    public ValuationResponse createSnapshot(UUID portfolioId, LocalDate valuationDate) {
        PortfolioValuationSnapshot snapshot = new PortfolioValuationSnapshot();
        snapshot.setPortfolio(portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", portfolioId)));
        snapshot.setValuationDate(valuationDate);
        snapshot.setTotalValue(BigDecimal.ZERO); // Stub
        snapshot.setCurrency(snapshot.getPortfolio().getBaseCurrency());

        PortfolioValuationSnapshot saved = snapshotRepository.save(snapshot);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ValuationResponse getLatestValuation(UUID portfolioId) {
        return snapshotRepository.findByPortfolioIdOrderByValuationDateDesc(portfolioId).stream()
                .findFirst()
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Latest Valuation for Portfolio", portfolioId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValuationResponse> getValuationHistory(UUID portfolioId) {
        return snapshotRepository.findByPortfolioIdOrderByValuationDateDesc(portfolioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ValuationResponse mapToResponse(PortfolioValuationSnapshot snapshot) {
        return ValuationResponse.builder()
                .portfolioId(snapshot.getPortfolio().getId())
                .valuationDate(snapshot.getValuationDate())
                .totalValue(snapshot.getTotalValue())
                .currency(snapshot.getCurrency().getIsoCode())
                .build();
    }
}
