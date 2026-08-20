package com.wealthlink.marketdata.service;

import com.wealthlink.marketdata.dto.FxRateSourceCreateRequest;
import com.wealthlink.marketdata.dto.FxRateSourceResponse;

import java.util.List;
import java.util.UUID;

public interface FxRateSourceService {

    FxRateSourceResponse create(
            FxRateSourceCreateRequest request
    );

    FxRateSourceResponse getById(UUID id);

    List<FxRateSourceResponse> getAll();

    FxRateSourceResponse getByCode(String code);

    void delete(UUID id);
}