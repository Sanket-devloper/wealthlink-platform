package com.wealthlink.reference.service;

import com.wealthlink.reference.dto.MarketRequest;
import com.wealthlink.reference.dto.MarketResponse;

import java.util.List;
import java.util.UUID;

public interface MarketService {
    List<MarketResponse> findAll();
    List<MarketResponse> findByCountry(UUID countryId);
    MarketResponse findById(UUID id);
    MarketResponse create(MarketRequest request);
    MarketResponse update(UUID id, MarketRequest request);
    void delete(UUID id);
}