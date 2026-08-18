package com.wealthlink.dividend.repository;

import com.wealthlink.dividend.entity.DividendAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DividendAllocationRepository extends JpaRepository<DividendAllocation, UUID> {
    List<DividendAllocation> findByDividendEventId(UUID dividendEventId);
    List<DividendAllocation> findByPortfolioId(UUID portfolioId);
}