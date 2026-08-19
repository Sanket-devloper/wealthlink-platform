package com.wealthlink.reference.service.impl;

import com.wealthlink.reference.service.MarketService;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.reference.dto.MarketRequest;
import com.wealthlink.reference.dto.MarketResponse;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Market;
import com.wealthlink.reference.entity.MarketStatus;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketServiceImpl implements MarketService {

    private final MarketRepository marketRepository;
    private final CountryRepository countryRepository;

    @Override
    public List<MarketResponse> findAll() {
        return marketRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<MarketResponse> findByCountry(UUID countryId) {
        return marketRepository.findByCountryId(countryId).stream().map(this::toResponse).toList();
    }

    @Override
    public MarketResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public MarketResponse create(MarketRequest request) {
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.countryId()));
        Market market = Market.builder()
                .country(country)
                .name(request.name())
                .micCode(request.micCode())
                .timezone(request.timezone())
                .status(request.status() != null ? request.status() : MarketStatus.ACTIVE)
                .build();
        return toResponse(marketRepository.save(market));
    }

    @Override
    public MarketResponse update(UUID id, MarketRequest request) {
        Market market = getOrThrow(id);
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.countryId()));
        market.setCountry(country);
        market.setName(request.name());
        market.setMicCode(request.micCode());
        market.setTimezone(request.timezone());
        if (request.status() != null) {
            market.setStatus(request.status());
        }
        return toResponse(marketRepository.save(market));
    }

    @Override
    public void delete(UUID id) {
        marketRepository.delete(getOrThrow(id));
    }

    private Market getOrThrow(UUID id) {
        return marketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Market", id));
    }

    private MarketResponse toResponse(Market m) {
        return new MarketResponse(
                m.getId(), m.getCountry().getId(), m.getCountry().getIsoCode(),
                m.getName(), m.getMicCode(), m.getTimezone(), m.getStatus());
    }
}
