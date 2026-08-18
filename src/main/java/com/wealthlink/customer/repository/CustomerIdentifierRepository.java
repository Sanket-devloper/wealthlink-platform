package com.wealthlink.customer.repository;

import com.wealthlink.customer.entity.CustomerIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerIdentifierRepository extends JpaRepository<CustomerIdentifier, UUID> {
    List<CustomerIdentifier> findByCustomerId(UUID customerId);
}
