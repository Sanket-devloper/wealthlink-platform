package com.wealthlink.reference.repository;

import com.wealthlink.reference.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CurrencyRepository extends JpaRepository<Currency, UUID> {
    Optional<Currency> findByIsoCode(String isoCode);
}
