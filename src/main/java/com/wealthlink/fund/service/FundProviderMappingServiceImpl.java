package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundProviderMappingCreateRequest;
import com.wealthlink.fund.dto.FundProviderMappingResponse;
import com.wealthlink.fund.entity.FundProviderMapping;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.exception.FundProviderMappingNotFoundException;
import com.wealthlink.fund.mapper.FundProviderMappingMapper;
import com.wealthlink.fund.repository.FundProviderMappingRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.fund.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundProviderMappingServiceImpl implements FundProviderMappingService {

    private final FundProviderMappingRepository fundProviderMappingRepository;
    private final FundShareClassRepository fundShareClassRepository;
    private final ProviderRepository providerRepository;
    private final FundProviderMappingMapper fundProviderMappingMapper;

    @Override
    public FundProviderMappingResponse create(
            FundProviderMappingCreateRequest request) {

        FundShareClass fundShareClass =
                fundShareClassRepository.findById(request.fundShareClassId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Fund Share Class not found with id: "
                                                + request.fundShareClassId()));

        Provider provider =
                providerRepository.findById(request.providerId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Provider not found with id: "
                                                + request.providerId()));

        FundProviderMapping mapping = FundProviderMapping.builder()
                .fundShareClass(fundShareClass)
                .provider(provider)
                .externalFundId(request.externalFundId())
                .build();

        FundProviderMapping saved =
                fundProviderMappingRepository.save(mapping);

        return fundProviderMappingMapper.toResponse(saved);
    }

    @Override
    public FundProviderMappingResponse getById(UUID id) {

        FundProviderMapping mapping =
                fundProviderMappingRepository.findById(id)
                        .orElseThrow(() ->
                                new FundProviderMappingNotFoundException(
                                        "Fund Provider Mapping not found with id: "
                                                + id));

        return fundProviderMappingMapper.toResponse(mapping);
    }

    @Override
    public List<FundProviderMappingResponse> getAll() {

        return fundProviderMappingRepository.findAll()
                .stream()
                .map(fundProviderMappingMapper::toResponse)
                .toList();
    }

    @Override
    public FundProviderMappingResponse getByProviderAndExternalFundId(
            UUID providerId,
            String externalFundId) {

        FundProviderMapping mapping =
                fundProviderMappingRepository
                        .findByProviderIdAndExternalFundId(
                                providerId,
                                externalFundId)
                        .orElseThrow(() ->
                                new FundProviderMappingNotFoundException(
                                        "Fund Provider Mapping not found for providerId: "
                                                + providerId
                                                + " and externalFundId: "
                                                + externalFundId));

        return fundProviderMappingMapper.toResponse(mapping);
    }

    @Override
    public void delete(UUID id) {

        FundProviderMapping mapping =
                fundProviderMappingRepository.findById(id)
                        .orElseThrow(() ->
                                new FundProviderMappingNotFoundException(
                                        "Fund Provider Mapping not found with id: "
                                                + id));

        fundProviderMappingRepository.delete(mapping);
    }
}