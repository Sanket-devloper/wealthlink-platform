package com.wealthlink.fund.service;

import com.wealthlink.fund.dto.FundShareClassCreateRequest;
import com.wealthlink.fund.dto.FundShareClassResponse;
import com.wealthlink.fund.dto.FundShareClassUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface FundShareClassService {

    FundShareClassResponse createShareClass(
            FundShareClassCreateRequest request
    );

    FundShareClassResponse getShareClassById(
            UUID shareClassId
    );

    List<FundShareClassResponse> getAllShareClasses();

    List<FundShareClassResponse> getShareClassesByFundId(
            UUID fundId
    );

    FundShareClassResponse updateShareClass(
            UUID shareClassId,
            FundShareClassUpdateRequest request
    );
}