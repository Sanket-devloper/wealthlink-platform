package com.wealthlink.fund.repository;

import com.wealthlink.fund.entity.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundRepository extends JpaRepository<Fund, UUID> {
    Optional<Fund> findByIsin(String isin);
}
