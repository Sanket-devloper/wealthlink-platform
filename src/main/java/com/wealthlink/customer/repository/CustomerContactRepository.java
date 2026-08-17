package com.wealthlink.customer.repository;

import com.wealthlink.customer.entity.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerContactRepository extends JpaRepository<CustomerContact, UUID> {
    List<CustomerContact> findByCustomerId(UUID customerId);
}
