package com.wealthlink.marketdata.repository;

import com.wealthlink.marketdata.entity.FxRate;
import com.wealthlink.marketdata.entity.RateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FxRateRepository extends JpaRepository<FxRate, UUID> {

    Optional<FxRate> findByBaseCurrencyIdAndQuoteCurrencyIdAndRateDateAndRateTypeAndSourceId(
            UUID baseCurrencyId, UUID quoteCurrencyId, LocalDate rateDate, RateType rateType, UUID sourceId);

    List<FxRate> findByBaseCurrencyIdAndQuoteCurrencyIdOrderByRateDateDesc(UUID baseCurrencyId, UUID quoteCurrencyId);
}
