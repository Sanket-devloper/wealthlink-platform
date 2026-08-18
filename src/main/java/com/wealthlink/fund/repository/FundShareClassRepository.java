package com.wealthlink.fund.repository;

import com.wealthlink.fund.entity.FundShareClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundShareClassRepository extends JpaRepository<FundShareClass, UUID> {
    List<FundShareClass> findByFundId(UUID fundId);

    Optional<FundShareClass> findByFundIdAndClassCode(UUID fundId, String classCode);
}
