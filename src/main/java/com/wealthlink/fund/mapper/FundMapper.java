package com.wealthlink.fund.mapper;

import com.wealthlink.fund.dto.FundResponse;
import com.wealthlink.fund.entity.Fund;
import org.springframework.stereotype.Component;

@Component
public class FundMapper {

    public FundResponse toResponse(Fund fund) {

        return new FundResponse(
                fund.getId(),
                fund.getIsin(),
                fund.getName(),
                fund.getBaseCurrency().getId(),
                fund.getDomicileCountry().getId(),
                fund.getStatus(),
                fund.getInceptionDate(),
                fund.getCreatedAt(),
                fund.getUpdatedAt()
        );
    }
}