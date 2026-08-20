package com.wealthlink.marketdata.service;

import com.wealthlink.marketdata.dto.FundPriceCreateRequest;
import com.wealthlink.marketdata.dto.FundPriceResponse;

import java.util.List;
import java.util.UUID;

public interface FundPriceService {

    FundPriceResponse create(FundPriceCreateRequest request);

    FundPriceResponse getById(UUID id);

    List<FundPriceResponse> getAll();

    List<FundPriceResponse> getByFundShareClass(UUID fundShareClassId);

    void delete(UUID id);
}