package com.wealthlink.marketdata.service;

import com.wealthlink.marketdata.dto.FxRateCreateRequest;
import com.wealthlink.marketdata.dto.FxRateResponse;

import java.util.List;
import java.util.UUID;

public interface FxRateService {

    FxRateResponse create(FxRateCreateRequest request);

    FxRateResponse getById(UUID id);

    List<FxRateResponse> getAll();

    List<FxRateResponse> getByCurrencyPair(
            UUID baseCurrencyId,
            UUID quoteCurrencyId
    );

    void delete(UUID id);
}