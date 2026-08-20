package com.wealthlink.marketdata.service.impl;

import com.wealthlink.marketdata.dto.FxRateSourceCreateRequest;
import com.wealthlink.marketdata.dto.FxRateSourceResponse;
import com.wealthlink.marketdata.entity.FxRateSource;
import com.wealthlink.marketdata.exception.FxRateSourceAlreadyExistsException;
import com.wealthlink.marketdata.exception.FxRateSourceNotFoundException;
import com.wealthlink.marketdata.mapper.FxRateSourceMapper;
import com.wealthlink.marketdata.repository.FxRateSourceRepository;
import com.wealthlink.marketdata.service.FxRateSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FxRateSourceServiceImpl implements FxRateSourceService {

    private final FxRateSourceRepository fxRateSourceRepository;
    private final FxRateSourceMapper fxRateSourceMapper;

    @Override
    public FxRateSourceResponse create(
            FxRateSourceCreateRequest request) {

        fxRateSourceRepository.findByCode(request.code())
                .ifPresent(existing -> {
                    throw new FxRateSourceAlreadyExistsException(
                            "FX Rate Source already exists with code: "
                                    + request.code()
                    );
                });

        FxRateSource source = FxRateSource.builder()
                .code(request.code())
                .name(request.name())
                .build();

        FxRateSource saved =
                fxRateSourceRepository.save(source);

        return fxRateSourceMapper.toResponse(saved);
    }

    @Override
    public FxRateSourceResponse getById(UUID id) {

        FxRateSource source =
                fxRateSourceRepository.findById(id)
                        .orElseThrow(() ->
                                new FxRateSourceNotFoundException(
                                        "FX Rate Source not found with id: "
                                                + id
                                ));

        return fxRateSourceMapper.toResponse(source);
    }

    @Override
    public List<FxRateSourceResponse> getAll() {

        return fxRateSourceRepository.findAll()
                .stream()
                .map(fxRateSourceMapper::toResponse)
                .toList();
    }

    @Override
    public FxRateSourceResponse getByCode(String code) {

        FxRateSource source =
                fxRateSourceRepository.findByCode(code)
                        .orElseThrow(() ->
                                new FxRateSourceNotFoundException(
                                        "FX Rate Source not found with code: "
                                                + code
                                ));

        return fxRateSourceMapper.toResponse(source);
    }

    @Override
    public void delete(UUID id) {

        FxRateSource source =
                fxRateSourceRepository.findById(id)
                        .orElseThrow(() ->
                                new FxRateSourceNotFoundException(
                                        "FX Rate Source not found with id: "
                                                + id
                                ));

        fxRateSourceRepository.delete(source);
    }
}