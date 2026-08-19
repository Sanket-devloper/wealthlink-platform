package com.wealthlink.marketdata.mapper;

import com.wealthlink.marketdata.dto.FxRateResponse;
import com.wealthlink.marketdata.entity.FxRate;
import org.springframework.stereotype.Component;

@Component
public class FxRateMapper {

    public FxRateResponse toResponse(FxRate fxRate) {

        return new FxRateResponse(
                fxRate.getId(),
                fxRate.getBaseCurrency().getId(),
                fxRate.getQuoteCurrency().getId(),
                fxRate.getRateDate(),
                fxRate.getRateType(),
                fxRate.getSource().getId(),
                fxRate.getRate()
        );
    }
}