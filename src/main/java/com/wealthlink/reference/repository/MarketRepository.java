package com.wealthlink.reference.repository;

import com.wealthlink.reference.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketRepository extends JpaRepository<Market, UUID> {
    List<Market> findByCountryId(UUID countryId);
}
