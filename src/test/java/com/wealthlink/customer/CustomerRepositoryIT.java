package com.wealthlink.customer;

import com.wealthlink.customer.entity.ContactType;
import com.wealthlink.customer.entity.Customer;
import com.wealthlink.customer.entity.CustomerContact;
import com.wealthlink.customer.entity.CustomerIdentifier;
import com.wealthlink.customer.entity.CustomerStatus;
import com.wealthlink.customer.entity.CustomerType;
import com.wealthlink.customer.repository.CustomerContactRepository;
import com.wealthlink.customer.repository.CustomerIdentifierRepository;
import com.wealthlink.customer.repository.CustomerRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer tests for Customer Management:
 * CUSTOMER, CUSTOMER_CONTACT, CUSTOMER_IDENTIFIER.
 */
class CustomerRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerContactRepository customerContactRepository;

    @Autowired
    private CustomerIdentifierRepository customerIdentifierRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Customer newCustomer(String customerNumber) {
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        return Customer.builder()
                .customerNumber(customerNumber)
                .customerType(CustomerType.INDIVIDUAL)
                .country(norway)
                .taxResidencyCountry(norway)
                .build();
    }

    @Test
    void savingACustomerDefaultsStatusAndTimestamps() {
        Customer saved = customerRepository.saveAndFlush(newCustomer("CUST-0001"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(CustomerStatus.PENDING_KYC); // @PrePersist default
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void customerNumberUniqueConstraintIsEnforced() {
        customerRepository.saveAndFlush(newCustomer("CUST-0002"));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(newCustomer("CUST-0002")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void customerCanHaveMultipleContactsAndIdentifiers() {
        Customer customer = customerRepository.saveAndFlush(newCustomer("CUST-0003"));
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        CustomerContact email = CustomerContact.builder()
                .customer(customer)
                .contactType(ContactType.EMAIL)
                .value("investor@example.com")
                .isPrimary(true)
                .build();
        customerContactRepository.saveAndFlush(email);

        CustomerIdentifier idDoc = CustomerIdentifier.builder()
                .customer(customer)
                .idType("PASSPORT")
                .idValue("N1234567")
                .issuingCountry(norway)
                .build();
        customerIdentifierRepository.saveAndFlush(idDoc);

        assertThat(customerContactRepository.findByCustomerId(customer.getId())).hasSize(1);
        assertThat(customerIdentifierRepository.findByCustomerId(customer.getId())).hasSize(1);
    }

    @Test
    void onlyOnePrimaryContactAllowedPerContactType() {
        Customer customer = customerRepository.saveAndFlush(newCustomer("CUST-0004"));

        customerContactRepository.saveAndFlush(CustomerContact.builder()
                .customer(customer)
                .contactType(ContactType.EMAIL)
                .value("first@example.com")
                .isPrimary(true)
                .build());

        CustomerContact secondPrimaryEmail = CustomerContact.builder()
                .customer(customer)
                .contactType(ContactType.EMAIL)
                .value("second@example.com")
                .isPrimary(true)
                .build();

        // Enforced by the partial unique index in V1__dev1_foundation.sql:
        // uq_customer_contact_primary on (customer_id, contact_type) where is_primary = true
        assertThatThrownBy(() -> customerContactRepository.saveAndFlush(secondPrimaryEmail))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
