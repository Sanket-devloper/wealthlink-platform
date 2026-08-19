package com.wealthlink.portfolio.service;

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
                .orElseThrow(() -> new RuntimeException("Snapshot not found"));
        return mapToResponse(snapshot);
    }

    @Override
    @Transactional
    public ValuationResponse createSnapshot(UUID portfolioId, LocalDate valuationDate) {
        PortfolioValuationSnapshot snapshot = new PortfolioValuationSnapshot();
        snapshot.setPortfolio(portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found")));
        snapshot.setValuationDate(valuationDate);
        snapshot.setTotalValue(BigDecimal.ZERO); // Stub
        snapshot.setCurrency(snapshot.getPortfolio().getBaseCurrency());

        PortfolioValuationSnapshot saved = snapshotRepository.save(snapshot);
        return mapToResponse(saved);
    }

    private ValuationResponse mapToResponse(PortfolioValuationSnapshot snapshot) {
        return ValuationResponse.builder()
                .id(snapshot.getId())
                .portfolioId(snapshot.getPortfolio().getId())
                .valuationDate(snapshot.getValuationDate())
                .totalValue(snapshot.getTotalValue())
                .currency(snapshot.getCurrency().getIsoCode())
                .build();
    }
}
