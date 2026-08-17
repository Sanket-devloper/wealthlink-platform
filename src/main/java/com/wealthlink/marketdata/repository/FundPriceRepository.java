package com.wealthlink.marketdata.repository;

import com.wealthlink.marketdata.entity.FundPrice;
import com.wealthlink.marketdata.entity.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundPriceRepository extends JpaRepository<FundPrice, UUID> {

    Optional<FundPrice> findByFundShareClassIdAndPriceDateAndPriceTypeAndProviderId(
            UUID fundShareClassId, LocalDate priceDate, PriceType priceType, UUID providerId);

    List<FundPrice> findByFundShareClassIdOrderByPriceDateDesc(UUID fundShareClassId);
}
