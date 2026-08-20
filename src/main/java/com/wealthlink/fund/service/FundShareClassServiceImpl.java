package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundShareClassCreateRequest;
import com.wealthlink.fund.dto.FundShareClassResponse;
import com.wealthlink.fund.dto.FundShareClassUpdateRequest;
import com.wealthlink.fund.entity.Fund;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.fund.exception.FundShareClassNotFoundException;
import com.wealthlink.fund.mapper.FundShareClassMapper;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FundShareClassServiceImpl implements FundShareClassService {

    private final FundShareClassRepository fundShareClassRepository;
    private final FundRepository fundRepository;
    private final CurrencyRepository currencyRepository;
    private final FundShareClassMapper fundShareClassMapper;

    public FundShareClassServiceImpl(
            FundShareClassRepository fundShareClassRepository,
            FundRepository fundRepository,
            CurrencyRepository currencyRepository,
            FundShareClassMapper fundShareClassMapper
    ) {
        this.fundShareClassRepository = fundShareClassRepository;
        this.fundRepository = fundRepository;
        this.currencyRepository = currencyRepository;
        this.fundShareClassMapper = fundShareClassMapper;
    }

    @Override
    public FundShareClassResponse createShareClass(
            FundShareClassCreateRequest request
    ) {

        Fund fund = fundRepository.findById(request.fundId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fund not found with id: " + request.fundId()
                ));

        Currency currency = currencyRepository.findById(request.currencyId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Currency not found with id: " + request.currencyId()
                ));

        // Check fundId + classCode uniqueness
        if (fundShareClassRepository
                .findByFundIdAndClassCode(
                        request.fundId(),
                        request.classCode()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Share class with class code '"
                            + request.classCode()
                            + "' already exists for fund: "
                            + request.fundId()
            );
        }

        FundShareClass entity = new FundShareClass();

        entity.setFund(fund);
        entity.setClassCode(request.classCode());
        entity.setName(request.name());
        entity.setCurrency(currency);
        entity.setStatus(request.status());

        FundShareClass savedEntity =
                fundShareClassRepository.save(entity);

        return fundShareClassMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public FundShareClassResponse getShareClassById(
            UUID shareClassId
    ) {

        FundShareClass entity = fundShareClassRepository
                .findById(shareClassId)
                .orElseThrow(() ->
                        new FundShareClassNotFoundException(shareClassId)
                );

        return fundShareClassMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundShareClassResponse> getAllShareClasses() {

        return fundShareClassRepository.findAll()
                .stream()
                .map(fundShareClassMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundShareClassResponse> getShareClassesByFundId(
            UUID fundId
    ) {

        return fundShareClassRepository.findByFundId(fundId)
                .stream()
                .map(fundShareClassMapper::toResponse)
                .toList();
    }

    @Override
    public FundShareClassResponse updateShareClass(
            UUID shareClassId,
            FundShareClassUpdateRequest request
    ) {

        FundShareClass entity = fundShareClassRepository
                .findById(shareClassId)
                .orElseThrow(() ->
                        new FundShareClassNotFoundException(shareClassId)
                );

        if (request.classCode() != null) {

            // Check uniqueness only when classCode is being changed
            if (!request.classCode().equals(entity.getClassCode())) {

                if (fundShareClassRepository
                        .findByFundIdAndClassCode(
                                entity.getFund().getId(),
                                request.classCode()
                        )
                        .isPresent()) {

                    throw new IllegalArgumentException(
                            "Share class with class code '"
                                    + request.classCode()
                                    + "' already exists for fund: "
                                    + entity.getFund().getId()
                    );
                }
            }

            entity.setClassCode(request.classCode());
        }

        if (request.name() != null) {
            entity.setName(request.name());
        }

        if (request.currencyId() != null) {

            Currency currency = currencyRepository
                    .findById(request.currencyId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Currency not found with id: "
                                    + request.currencyId()
                    ));

            entity.setCurrency(currency);
        }

        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        FundShareClass updatedEntity =
                fundShareClassRepository.save(entity);

        return fundShareClassMapper.toResponse(updatedEntity);
    }
}