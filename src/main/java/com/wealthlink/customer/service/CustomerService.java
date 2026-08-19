package com.wealthlink.customer.service;

import com.wealthlink.customer.dto.*;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<CustomerResponse> findAll();
    CustomerResponse findById(UUID id);
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(UUID id, CustomerRequest request);
    void delete(UUID id);
    List<CustomerContactResponse> findContacts(UUID customerId);
    CustomerContactResponse addContact(UUID customerId, CustomerContactRequest request);
    List<CustomerIdentifierResponse> findIdentifiers(UUID customerId);
    CustomerIdentifierResponse addIdentifier(UUID customerId, CustomerIdentifierRequest request);
}