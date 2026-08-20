package com.wealthlink.fund.repository;

import com.wealthlink.fund.entity.Fund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundRepository extends JpaRepository<Fund, UUID> {

    Optional<Fund> findByIsin(String isin);

    boolean existsByIsin(String isin);

    Page<Fund> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}