package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundCreateRequest;
import com.wealthlink.fund.dto.FundResponse;
import com.wealthlink.fund.dto.FundUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FundService {

    FundResponse createFund(FundCreateRequest request);

    FundResponse getFund(UUID fundId);

    Page<FundResponse> getFunds(
            String isin,
            String name,
            Pageable pageable
    );

    FundResponse updateFund(
            UUID fundId,
            FundUpdateRequest request
    );
}