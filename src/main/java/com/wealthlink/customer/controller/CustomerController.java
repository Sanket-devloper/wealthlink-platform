package com.wealthlink.customer.controller;

import com.wealthlink.customer.dto.*;
import com.wealthlink.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public List<CustomerResponse> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable UUID id) {
        return customerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        customerService.delete(id);
    }

    @GetMapping("/{id}/contacts")
    public List<CustomerContactResponse> findContacts(@PathVariable UUID id) {
        return customerService.findContacts(id);
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<CustomerContactResponse> addContact(@PathVariable UUID id,
                                                                @Valid @RequestBody CustomerContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addContact(id, request));
    }

    @GetMapping("/{id}/identifiers")
    public List<CustomerIdentifierResponse> findIdentifiers(@PathVariable UUID id) {
        return customerService.findIdentifiers(id);
    }

    @PostMapping("/{id}/identifiers")
    public ResponseEntity<CustomerIdentifierResponse> addIdentifier(@PathVariable UUID id,
                                                                      @Valid @RequestBody CustomerIdentifierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addIdentifier(id, request));
    }
}
