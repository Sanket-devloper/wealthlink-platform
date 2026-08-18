package com.wealthlink.account.controller;

import com.wealthlink.account.dto.*;
import com.wealthlink.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        AccountResponse created = accountService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable UUID id, @Valid @RequestBody AccountRequest request) {
        return accountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        accountService.delete(id);
    }

    @GetMapping("/{id}/owners")
    public List<AccountOwnerResponse> findOwners(@PathVariable UUID id) {
        return accountService.findOwners(id);
    }

    @PostMapping("/{id}/owners")
    public ResponseEntity<AccountOwnerResponse> addOwner(@PathVariable UUID id,
                                                           @Valid @RequestBody AccountOwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.addOwner(id, request));
    }
}
