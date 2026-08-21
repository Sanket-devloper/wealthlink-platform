package com.wealthlink.dividend.repository;

import com.wealthlink.dividend.entity.DividendAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DividendAllocationRepository extends JpaRepository<DividendAllocation, UUID> {

    @Query("SELECT da FROM DividendAllocation da " +
            "LEFT JOIN FETCH da.dividendEvent " +
            "LEFT JOIN FETCH da.portfolio " +
            "LEFT JOIN FETCH da.currency " +
            "WHERE da.dividendEvent.id = :eventId")
    List<DividendAllocation> findByDividendEventId(@Param("eventId") UUID eventId);

    @Query("SELECT da FROM DividendAllocation da " +
            "LEFT JOIN FETCH da.dividendEvent " +
            "LEFT JOIN FETCH da.portfolio " +
            "LEFT JOIN FETCH da.currency " +
            "WHERE da.portfolio.id = :portfolioId")
    List<DividendAllocation> findByPortfolioId(@Param("portfolioId") UUID portfolioId);
}