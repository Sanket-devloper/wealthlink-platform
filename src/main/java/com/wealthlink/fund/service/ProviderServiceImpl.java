package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.ProviderCreateRequest;
import com.wealthlink.fund.dto.ProviderResponse;
import com.wealthlink.fund.dto.ProviderUpdateRequest;
import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.exception.ProviderNotFoundException;
import com.wealthlink.fund.mapper.ProviderMapper;
import com.wealthlink.fund.repository.ProviderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    public ProviderServiceImpl(
            ProviderRepository providerRepository,
            ProviderMapper providerMapper
    ) {
        this.providerRepository = providerRepository;
        this.providerMapper = providerMapper;
    }

    @Override
    public ProviderResponse createProvider(
            ProviderCreateRequest request
    ) {

        providerRepository.findByCode(request.code())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Provider with code '" + request.code()
                                    + "' already exists"
                    );
                });

        Provider entity = new Provider();

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setStatus(request.status());

        Provider savedEntity =
                providerRepository.save(entity);

        return providerMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(
            UUID providerId
    ) {

        Provider entity = providerRepository
                .findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(providerId)
                );

        return providerMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getAllProviders() {

        return providerRepository.findAll()
                .stream()
                .map(providerMapper::toResponse)
                .toList();
    }

    @Override
    public ProviderResponse updateProvider(
            UUID providerId,
            ProviderUpdateRequest request
    ) {

        Provider entity = providerRepository
                .findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(providerId)
                );

        if (request.code() != null) {

            providerRepository.findByCode(request.code())
                    .ifPresent(existing -> {

                        if (!existing.getId().equals(providerId)) {
                            throw new IllegalArgumentException(
                                    "Provider with code '"
                                            + request.code()
                                            + "' already exists"
                            );
                        }
                    });

            entity.setCode(request.code());
        }

        if (request.name() != null) {
            entity.setName(request.name());
        }

        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        Provider updatedEntity =
                providerRepository.save(entity);

        return providerMapper.toResponse(updatedEntity);
    }
}