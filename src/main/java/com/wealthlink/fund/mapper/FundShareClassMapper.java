package com.wealthlink.fund.mapper;

import com.wealthlink.fund.dto.FundShareClassResponse;
import com.wealthlink.fund.entity.FundShareClass;
import org.springframework.stereotype.Component;

@Component
public class FundShareClassMapper {

    public FundShareClassResponse toResponse(FundShareClass entity) {
        return new FundShareClassResponse(
                entity.getId(),
                entity.getFund().getId(),
                entity.getClassCode(),
                entity.getName(),
                entity.getCurrency().getId(),
                entity.getStatus()
        );
    }
}