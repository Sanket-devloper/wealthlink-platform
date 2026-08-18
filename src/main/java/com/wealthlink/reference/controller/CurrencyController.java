package com.wealthlink.reference.controller;

import com.wealthlink.reference.dto.CurrencyRequest;
import com.wealthlink.reference.dto.CurrencyResponse;
import com.wealthlink.reference.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public List<CurrencyResponse> findAll() {
        return currencyService.findAll();
    }

    @GetMapping("/{id}")
    public CurrencyResponse findById(@PathVariable UUID id) {
        return currencyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CurrencyResponse> create(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse created = currencyService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/currencies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CurrencyResponse update(@PathVariable UUID id, @Valid @RequestBody CurrencyRequest request) {
        return currencyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        currencyService.delete(id);
    }
}
