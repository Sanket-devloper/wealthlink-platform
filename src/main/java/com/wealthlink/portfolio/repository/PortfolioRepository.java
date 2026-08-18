package com.wealthlink.portfolio.repository;

import com.wealthlink.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    Optional<Portfolio> findByPortfolioNumber(String portfolioNumber);
}
