package com.wealthlink.portfolio.repository;

import com.wealthlink.portfolio.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    Optional<Position> findByPortfolioIdAndFundShareClassIdAndPositionDate(UUID portfolioId, UUID fundShareClassId, LocalDate positionDate);

    // Added: Fetch all investor positions for a specific fund share class as of recordDate
    List<Position> findByFundShareClassIdAndPositionDate(UUID fundShareClassId, LocalDate positionDate);
    
    List<Position> findByPortfolioId(UUID portfolioId);
}
