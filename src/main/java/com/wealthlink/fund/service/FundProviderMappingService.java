package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundProviderMappingCreateRequest;
import com.wealthlink.fund.dto.FundProviderMappingResponse;

import java.util.List;
import java.util.UUID;

public interface FundProviderMappingService {

    FundProviderMappingResponse create(
            FundProviderMappingCreateRequest request
    );

    FundProviderMappingResponse getById(UUID id);

    List<FundProviderMappingResponse> getAll();

    FundProviderMappingResponse getByProviderAndExternalFundId(
            UUID providerId,
            String externalFundId
    );

    void delete(UUID id);
}