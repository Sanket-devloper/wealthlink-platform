package com.wealthlink.reference.service;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.reference.dto.CountryRequest;
import com.wealthlink.reference.dto.CountryResponse;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CountryService {

    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;

    public List<CountryResponse> findAll() {
        return countryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CountryResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CountryResponse create(CountryRequest request) {
        Currency currency = currencyRepository.findById(request.defaultCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.defaultCurrencyId()));
        Country country = Country.builder()
                .isoCode(request.isoCode().toUpperCase())
                .name(request.name())
                .defaultCurrency(currency)
                .timezone(request.timezone())
                .build();
        return toResponse(countryRepository.save(country));
    }

    public CountryResponse update(UUID id, CountryRequest request) {
        Country country = getOrThrow(id);
        Currency currency = currencyRepository.findById(request.defaultCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.defaultCurrencyId()));
        country.setIsoCode(request.isoCode().toUpperCase());
        country.setName(request.name());
        country.setDefaultCurrency(currency);
        country.setTimezone(request.timezone());
        return toResponse(countryRepository.save(country));
    }

    public void delete(UUID id) {
        countryRepository.delete(getOrThrow(id));
    }

    private Country getOrThrow(UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private CountryResponse toResponse(Country c) {
        return new CountryResponse(
                c.getId(), c.getIsoCode(), c.getName(),
                c.getDefaultCurrency().getId(), c.getDefaultCurrency().getIsoCode(),
                c.getTimezone());
    }
}
