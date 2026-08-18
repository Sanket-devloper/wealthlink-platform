package com.wealthlink.portfolio.repository;

import com.wealthlink.portfolio.entity.PortfolioValuationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioValuationSnapshotRepository extends JpaRepository<PortfolioValuationSnapshot, UUID> {
    Optional<PortfolioValuationSnapshot> findByPortfolioIdAndValuationDate(UUID portfolioId, LocalDate valuationDate);
}
