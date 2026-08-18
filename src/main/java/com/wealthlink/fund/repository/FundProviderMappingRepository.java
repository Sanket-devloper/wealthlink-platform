package com.wealthlink.fund.repository;

import com.wealthlink.fund.entity.FundProviderMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundProviderMappingRepository extends JpaRepository<FundProviderMapping, UUID> {
    Optional<FundProviderMapping> findByProviderIdAndExternalFundId(UUID providerId, String externalFundId);
}
