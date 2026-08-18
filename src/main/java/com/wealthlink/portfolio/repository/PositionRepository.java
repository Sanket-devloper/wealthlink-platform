package com.wealthlink.portfolio.repository;

import com.wealthlink.portfolio.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    Optional<Position> findByPortfolioIdAndFundShareClassIdAndPositionDate(UUID portfolioId, UUID fundShareClassId, LocalDate positionDate);
}
