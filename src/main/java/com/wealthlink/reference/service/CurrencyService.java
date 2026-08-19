package com.wealthlink.reference.service;

import com.wealthlink.reference.dto.CurrencyRequest;
import com.wealthlink.reference.dto.CurrencyResponse;

import java.util.List;
import java.util.UUID;

public interface CurrencyService {
    List<CurrencyResponse> findAll();
    CurrencyResponse findById(UUID id);
    CurrencyResponse create(CurrencyRequest request);
    CurrencyResponse update(UUID id, CurrencyRequest request);
    void delete(UUID id);
}