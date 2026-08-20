package com.wealthlink.reference.service;

import com.wealthlink.reference.dto.CountryRequest;
import com.wealthlink.reference.dto.CountryResponse;

import java.util.List;
import java.util.UUID;

public interface CountryService {
    List<CountryResponse> findAll();
    CountryResponse findById(UUID id);
    CountryResponse create(CountryRequest request);
    CountryResponse update(UUID id, CountryRequest request);
    void delete(UUID id);
}