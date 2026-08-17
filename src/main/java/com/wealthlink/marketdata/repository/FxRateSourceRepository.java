package com.wealthlink.marketdata.repository;

import com.wealthlink.marketdata.entity.FxRateSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FxRateSourceRepository extends JpaRepository<FxRateSource, UUID> {
    Optional<FxRateSource> findByCode(String code);
}
