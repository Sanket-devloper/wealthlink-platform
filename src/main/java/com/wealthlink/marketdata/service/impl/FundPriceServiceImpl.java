package com.wealthlink.marketdata.service.impl;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.importdata.entity.ImportBatch;
import com.wealthlink.importdata.repository.ImportBatchRepository;
import com.wealthlink.marketdata.dto.FundPriceCreateRequest;
import com.wealthlink.marketdata.dto.FundPriceResponse;
import com.wealthlink.marketdata.entity.FundPrice;
import com.wealthlink.marketdata.exception.FundPriceAlreadyExistsException;
import com.wealthlink.marketdata.exception.FundPriceNotFoundException;
import com.wealthlink.marketdata.mapper.FundPriceMapper;
import com.wealthlink.marketdata.repository.FundPriceRepository;
import com.wealthlink.marketdata.service.FundPriceService;
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
public class FundPriceServiceImpl implements FundPriceService {

    private final FundPriceRepository fundPriceRepository;
    private final FundShareClassRepository fundShareClassRepository;
    private final ProviderRepository providerRepository;
    private final CurrencyRepository currencyRepository;
    private final ImportBatchRepository importBatchRepository;
    private final FundPriceMapper fundPriceMapper;

    @Override
    public FundPriceResponse create(FundPriceCreateRequest request) {

        FundShareClass fundShareClass = fundShareClassRepository
                .findById(request.fundShareClassId())
                .orElseThrow(() -> new FundPriceNotFoundException(
                        "Fund share class not found: " + request.fundShareClassId()
                ));

        Provider provider = providerRepository
                .findById(request.providerId())
                .orElseThrow(() -> new FundPriceNotFoundException(
                        "Provider not found: " + request.providerId()
                ));

        Currency currency = currencyRepository
                .findById(request.currencyId())
                .orElseThrow(() -> new FundPriceNotFoundException(
                        "Currency not found: " + request.currencyId()
                ));

        ImportBatch importBatch = null;

        if (request.importBatchId() != null) {
            importBatch = importBatchRepository
                    .findById(request.importBatchId())
                    .orElseThrow(() -> new FundPriceNotFoundException(
                            "Import batch not found: " + request.importBatchId()
                    ));
        }

        boolean alreadyExists = fundPriceRepository
                .findByFundShareClassIdAndPriceDateAndPriceTypeAndProviderId(
                        request.fundShareClassId(),
                        request.priceDate(),
                        request.priceType(),
                        request.providerId()
                )
                .isPresent();

        if (alreadyExists) {
            throw new FundPriceAlreadyExistsException(
                    "Fund price already exists for fund share class: "
                            + request.fundShareClassId()
                            + ", price date: "
                            + request.priceDate()
                            + ", price type: "
                            + request.priceType()
                            + ", provider: "
                            + request.providerId()
            );
        }

        FundPrice fundPrice = FundPrice.builder()
                .fundShareClass(fundShareClass)
                .priceDate(request.priceDate())
                .priceType(request.priceType())
                .provider(provider)
                .currency(currency)
                .price(request.price())
                .importBatch(importBatch)
                .build();

        FundPrice savedFundPrice = fundPriceRepository.save(fundPrice);

        return fundPriceMapper.toResponse(savedFundPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public FundPriceResponse getById(UUID id) {

        FundPrice fundPrice = fundPriceRepository.findById(id)
                .orElseThrow(() -> new FundPriceNotFoundException(
                        "Fund price not found: " + id
                ));

        return fundPriceMapper.toResponse(fundPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundPriceResponse> getAll() {

        return fundPriceRepository.findAll()
                .stream()
                .map(fundPriceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundPriceResponse> getByFundShareClass(UUID fundShareClassId) {

        return fundPriceRepository
                .findByFundShareClassIdOrderByPriceDateDesc(fundShareClassId)
                .stream()
                .map(fundPriceMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {

        FundPrice fundPrice = fundPriceRepository.findById(id)
                .orElseThrow(() -> new FundPriceNotFoundException(
                        "Fund price not found: " + id
                ));

        fundPriceRepository.delete(fundPrice);
    }
}