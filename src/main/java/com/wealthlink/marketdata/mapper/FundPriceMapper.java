package com.wealthlink.marketdata.mapper;

import com.wealthlink.marketdata.dto.FundPriceResponse;
import com.wealthlink.marketdata.entity.FundPrice;
import org.springframework.stereotype.Component;

@Component
public class FundPriceMapper {

    public FundPriceResponse toResponse(FundPrice fundPrice) {

        return new FundPriceResponse(
                fundPrice.getId(),
                fundPrice.getFundShareClass().getId(),
                fundPrice.getPriceDate(),
                fundPrice.getPriceType(),
                fundPrice.getProvider().getId(),
                fundPrice.getCurrency().getId(),
                fundPrice.getPrice(),
                fundPrice.getImportBatch() != null
                        ? fundPrice.getImportBatch().getId()
                        : null
        );
    }
}