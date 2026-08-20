package com.wealthlink.fund.mapper;

import com.wealthlink.fund.dto.FundProviderMappingResponse;
import com.wealthlink.fund.entity.FundProviderMapping;
import org.springframework.stereotype.Component;

@Component
public class FundProviderMappingMapper {

    public FundProviderMappingResponse toResponse(FundProviderMapping mapping) {

        return new FundProviderMappingResponse(
                mapping.getId(),
                mapping.getFundShareClass().getId(),
                mapping.getProvider().getId(),
                mapping.getExternalFundId()
        );
    }
}