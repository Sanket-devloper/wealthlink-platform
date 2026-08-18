package com.wealthlink.customer.service;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.customer.dto.*;
import com.wealthlink.customer.entity.Customer;
import com.wealthlink.customer.entity.CustomerContact;
import com.wealthlink.customer.entity.CustomerIdentifier;
import com.wealthlink.customer.repository.CustomerContactRepository;
import com.wealthlink.customer.repository.CustomerIdentifierRepository;
import com.wealthlink.customer.repository.CustomerRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;
    private final CustomerIdentifierRepository customerIdentifierRepository;
    private final CountryRepository countryRepository;

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CustomerResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Country country = findCountry(request.countryId());
        Country taxCountry = findCountry(request.taxResidencyCountryId());

        Customer customer = Customer.builder()
                .customerNumber(request.customerNumber())
                .customerType(request.customerType())
                .status(request.status())
                .country(country)
                .taxResidencyCountry(taxCountry)
                .build();
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getOrThrow(id);
        customer.setCustomerNumber(request.customerNumber());
        customer.setCustomerType(request.customerType());
        if (request.status() != null) {
            customer.setStatus(request.status());
        }
        customer.setCountry(findCountry(request.countryId()));
        customer.setTaxResidencyCountry(findCountry(request.taxResidencyCountryId()));
        return toResponse(customerRepository.save(customer));
    }

    public void delete(UUID id) {
        customerRepository.delete(getOrThrow(id));
    }

    public List<CustomerContactResponse> findContacts(UUID customerId) {
        return customerContactRepository.findByCustomerId(customerId).stream()
                .map(this::toContactResponse).toList();
    }

    public CustomerContactResponse addContact(UUID customerId, CustomerContactRequest request) {
        Customer customer = getOrThrow(customerId);
        CustomerContact contact = CustomerContact.builder()
                .customer(customer)
                .contactType(request.contactType())
                .value(request.value())
                .isPrimary(request.isPrimary())
                .build();
        return toContactResponse(customerContactRepository.save(contact));
    }

    public List<CustomerIdentifierResponse> findIdentifiers(UUID customerId) {
        return customerIdentifierRepository.findByCustomerId(customerId).stream()
                .map(this::toIdentifierResponse).toList();
    }

    public CustomerIdentifierResponse addIdentifier(UUID customerId, CustomerIdentifierRequest request) {
        Customer customer = getOrThrow(customerId);
        Country issuingCountry = findCountry(request.issuingCountryId());
        CustomerIdentifier identifier = CustomerIdentifier.builder()
                .customer(customer)
                .idType(request.idType())
                .idValue(request.idValue())
                .issuingCountry(issuingCountry)
                .build();
        return toIdentifierResponse(customerIdentifierRepository.save(identifier));
    }

    private Customer getOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private Country findCountry(UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getCustomerNumber(), c.getCustomerType(), c.getStatus(),
                c.getCountry().getId(), c.getCountry().getIsoCode(),
                c.getTaxResidencyCountry().getId(), c.getTaxResidencyCountry().getIsoCode(),
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private CustomerContactResponse toContactResponse(CustomerContact cc) {
        return new CustomerContactResponse(cc.getId(), cc.getCustomer().getId(),
                cc.getContactType(), cc.getValue(), cc.getIsPrimary());
    }

    private CustomerIdentifierResponse toIdentifierResponse(CustomerIdentifier ci) {
        return new CustomerIdentifierResponse(ci.getId(), ci.getCustomer().getId(),
                ci.getIdType(), ci.getIdValue(), ci.getIssuingCountry().getId(), ci.getIssuingCountry().getIsoCode());
    }
}
