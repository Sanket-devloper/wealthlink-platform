package com.wealthlink.marketdata.service.impl;

import com.wealthlink.marketdata.dto.FxRateCreateRequest;
import com.wealthlink.marketdata.dto.FxRateResponse;
import com.wealthlink.marketdata.entity.FxRate;
import com.wealthlink.marketdata.exception.FxRateAlreadyExistsException;
import com.wealthlink.marketdata.exception.FxRateNotFoundException;
import com.wealthlink.marketdata.mapper.FxRateMapper;
import com.wealthlink.marketdata.repository.FxRateRepository;
import com.wealthlink.marketdata.service.FxRateService;
import com.wealthlink.marketdata.repository.FxRateSourceRepository;
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
public class FxRateServiceImpl implements FxRateService {

    private final FxRateRepository fxRateRepository;
    private final CurrencyRepository currencyRepository;
    private final FxRateSourceRepository fxRateSourceRepository;
    private final FxRateMapper fxRateMapper;

    @Override
    public FxRateResponse create(FxRateCreateRequest request) {

        Currency baseCurrency = currencyRepository.findById(request.baseCurrencyId())
                .orElseThrow(() -> new FxRateNotFoundException(
                        "Base currency not found: " + request.baseCurrencyId()
                ));

        Currency quoteCurrency = currencyRepository.findById(request.quoteCurrencyId())
                .orElseThrow(() -> new FxRateNotFoundException(
                        "Quote currency not found: " + request.quoteCurrencyId()
                ));

        var source = fxRateSourceRepository.findById(request.sourceId())
                .orElseThrow(() -> new FxRateNotFoundException(
                        "FX rate source not found: " + request.sourceId()
                ));

        boolean alreadyExists = fxRateRepository
                .findByBaseCurrencyIdAndQuoteCurrencyIdAndRateDateAndRateTypeAndSourceId(
                        request.baseCurrencyId(),
                        request.quoteCurrencyId(),
                        request.rateDate(),
                        request.rateType(),
                        request.sourceId()
                )
                .isPresent();

        if (alreadyExists) {
            throw new FxRateAlreadyExistsException(
                    "FX rate already exists for base currency: "
                            + request.baseCurrencyId()
                            + ", quote currency: "
                            + request.quoteCurrencyId()
                            + ", rate date: "
                            + request.rateDate()
                            + ", rate type: "
                            + request.rateType()
                            + ", source: "
                            + request.sourceId()
            );
        }

        FxRate fxRate = FxRate.builder()
                .baseCurrency(baseCurrency)
                .quoteCurrency(quoteCurrency)
                .rateDate(request.rateDate())
                .rateType(request.rateType())
                .source(source)
                .rate(request.rate())
                .build();

        FxRate savedFxRate = fxRateRepository.save(fxRate);

        return fxRateMapper.toResponse(savedFxRate);
    }

    @Override
    @Transactional(readOnly = true)
    public FxRateResponse getById(UUID id) {

        FxRate fxRate = fxRateRepository.findById(id)
                .orElseThrow(() -> new FxRateNotFoundException(
                        "FX rate not found: " + id
                ));

        return fxRateMapper.toResponse(fxRate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FxRateResponse> getAll() {

        return fxRateRepository.findAll()
                .stream()
                .map(fxRateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FxRateResponse> getByCurrencyPair(
            UUID baseCurrencyId,
            UUID quoteCurrencyId) {

        return fxRateRepository
                .findByBaseCurrencyIdAndQuoteCurrencyIdOrderByRateDateDesc(
                        baseCurrencyId,
                        quoteCurrencyId
                )
                .stream()
                .map(fxRateMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {

        FxRate fxRate = fxRateRepository.findById(id)
                .orElseThrow(() -> new FxRateNotFoundException(
                        "FX rate not found: " + id
                ));

        fxRateRepository.delete(fxRate);
    }
}