package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundCreateRequest;
import com.wealthlink.fund.dto.FundResponse;
import com.wealthlink.fund.dto.FundUpdateRequest;
import com.wealthlink.fund.entity.Fund;
import com.wealthlink.fund.exception.CountryNotFoundException;
import com.wealthlink.fund.exception.CurrencyNotFoundException;
import com.wealthlink.fund.exception.DuplicateFundException;
import com.wealthlink.fund.exception.FundNotFoundException;
import com.wealthlink.fund.mapper.FundMapper;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepository;
    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final FundMapper fundMapper;

    @Override
    public FundResponse createFund(FundCreateRequest request) {

        // 1. Check duplicate ISIN
        if (fundRepository.existsByIsin(request.isin())) {
            throw new DuplicateFundException(request.isin());
        }

        // 2. Find base currency
        Currency currency = currencyRepository
                .findById(request.baseCurrencyId())
                .orElseThrow(() ->
                        new CurrencyNotFoundException(
                                request.baseCurrencyId()
                        )
                );

        // 3. Find domicile country
        Country country = countryRepository
                .findById(request.domicileCountryId())
                .orElseThrow(() ->
                        new CountryNotFoundException(
                                request.domicileCountryId()
                        )
                );

        // 4. Build Fund entity
        Fund fund = Fund.builder()
                .isin(request.isin())
                .name(request.name())
                .baseCurrency(currency)
                .domicileCountry(country)
                .inceptionDate(request.inceptionDate())
                .build();

        // 5. Save
        Fund savedFund = fundRepository.save(fund);

        // 6. Convert entity to response DTO
        return fundMapper.toResponse(savedFund);
    }

    @Override
    @Transactional
    public FundResponse getFund(UUID fundId) {

        Fund fund = fundRepository
                .findById(fundId)
                .orElseThrow(() ->
                        new FundNotFoundException(fundId)
                );

        return fundMapper.toResponse(fund);
    }

    @Override
    @Transactional
    public Page<FundResponse> getFunds(
            String isin,
            String name,
            Pageable pageable
    ) {

        Page<Fund> funds;

        // Search by exact ISIN
        if (isin != null && !isin.isBlank()) {

            Fund fund = fundRepository
                    .findByIsin(isin)
                    .orElse(null);

            if (fund == null) {
                return Page.empty(pageable);
            }

            funds = new PageImpl<>(
                    List.of(fund),
                    pageable,
                    1
            );

        }
        // Search by fund name
        else if (name != null && !name.isBlank()) {

            funds = fundRepository
                    .findByNameContainingIgnoreCase(
                            name,
                            pageable
                    );

        }
        // Return all funds
        else {

            funds = fundRepository.findAll(pageable);
        }

        return funds.map(fundMapper::toResponse);
    }

    @Override
    public FundResponse updateFund(
            UUID fundId,
            FundUpdateRequest request
    ) {

        // 1. Find existing fund
        Fund fund = fundRepository
                .findById(fundId)
                .orElseThrow(() ->
                        new FundNotFoundException(fundId)
                );

        // 2. Update name if supplied
        if (request.name() != null) {
            fund.setName(request.name());
        }

        // 3. Update base currency if supplied
        if (request.baseCurrencyId() != null) {

            Currency currency = currencyRepository
                    .findById(request.baseCurrencyId())
                    .orElseThrow(() ->
                            new CurrencyNotFoundException(
                                    request.baseCurrencyId()
                            )
                    );

            fund.setBaseCurrency(currency);
        }

        // 4. Update domicile country if supplied
        if (request.domicileCountryId() != null) {

            Country country = countryRepository
                    .findById(request.domicileCountryId())
                    .orElseThrow(() ->
                            new CountryNotFoundException(
                                    request.domicileCountryId()
                            )
                    );

            fund.setDomicileCountry(country);
        }

        // 5. Update status if supplied
        if (request.status() != null) {
            fund.setStatus(request.status());
        }

        // 6. Update inception date if supplied
        if (request.inceptionDate() != null) {
            fund.setInceptionDate(request.inceptionDate());
        }

        // 7. Save updated entity
        Fund updatedFund = fundRepository.save(fund);

        // 8. Return response DTO
        return fundMapper.toResponse(updatedFund);
    }
}