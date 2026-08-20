package com.wealthlink.marketdata.mapper;

import com.wealthlink.marketdata.dto.FxRateSourceResponse;
import com.wealthlink.marketdata.entity.FxRateSource;
import org.springframework.stereotype.Component;

@Component
public class FxRateSourceMapper {

    public FxRateSourceResponse toResponse(FxRateSource source) {

        return new FxRateSourceResponse(
                source.getId(),
                source.getCode(),
                source.getName()
        );
    }
}