package com.wealthlink.dividend.repository;

import com.wealthlink.dividend.entity.DividendEvent;
import com.wealthlink.dividend.entity.DividendEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DividendEventRepository extends JpaRepository<DividendEvent, UUID> {
    List<DividendEvent> findByFundShareClassId(UUID fundShareClassId);
    List<DividendEvent> findByStatus(DividendEventStatus status);
}