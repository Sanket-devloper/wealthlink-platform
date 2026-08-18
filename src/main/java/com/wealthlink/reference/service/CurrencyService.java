package com.wealthlink.reference.service;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.reference.dto.CurrencyRequest;
import com.wealthlink.reference.dto.CurrencyResponse;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public List<CurrencyResponse> findAll() {
        return currencyRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CurrencyResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CurrencyResponse create(CurrencyRequest request) {
        Currency currency = Currency.builder()
                .isoCode(request.isoCode().toUpperCase())
                .name(request.name())
                .minorUnitDigits(request.minorUnitDigits())
                .build();
        return toResponse(currencyRepository.save(currency));
    }

    public CurrencyResponse update(UUID id, CurrencyRequest request) {
        Currency currency = getOrThrow(id);
        currency.setIsoCode(request.isoCode().toUpperCase());
        currency.setName(request.name());
        currency.setMinorUnitDigits(request.minorUnitDigits());
        return toResponse(currencyRepository.save(currency));
    }

    public void delete(UUID id) {
        Currency currency = getOrThrow(id);
        currencyRepository.delete(currency);
    }

    private Currency getOrThrow(UUID id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    private CurrencyResponse toResponse(Currency c) {
        return new CurrencyResponse(c.getId(), c.getIsoCode(), c.getName(), c.getMinorUnitDigits());
    }
}
