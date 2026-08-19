package com.wealthlink.portfolio.service;

import com.wealthlink.portfolio.dto.ValuationResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioValuationSnapshotService {

    List<ValuationResponse> getAll();

    ValuationResponse getById(UUID id);

    ValuationResponse createSnapshot(UUID portfolioId, LocalDate valuationDate);
}
