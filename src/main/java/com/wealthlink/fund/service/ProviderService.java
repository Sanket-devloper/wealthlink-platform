package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.ProviderCreateRequest;
import com.wealthlink.fund.dto.ProviderResponse;
import com.wealthlink.fund.dto.ProviderUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ProviderService {

    ProviderResponse createProvider(
            ProviderCreateRequest request
    );

    ProviderResponse getProviderById(
            UUID providerId
    );

    List<ProviderResponse> getAllProviders();

    ProviderResponse updateProvider(
            UUID providerId,
            ProviderUpdateRequest request
    );
}