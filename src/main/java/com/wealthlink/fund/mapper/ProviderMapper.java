package com.wealthlink.fund.mapper;

import com.wealthlink.fund.dto.ProviderResponse;
import com.wealthlink.fund.entity.Provider;
import org.springframework.stereotype.Component;

@Component
public class ProviderMapper {

    public ProviderResponse toResponse(Provider entity) {
        return new ProviderResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}