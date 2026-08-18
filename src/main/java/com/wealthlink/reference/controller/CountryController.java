package com.wealthlink.reference.controller;

import com.wealthlink.reference.dto.CountryRequest;
import com.wealthlink.reference.dto.CountryResponse;
import com.wealthlink.reference.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public List<CountryResponse> findAll() {
        return countryService.findAll();
    }

    @GetMapping("/{id}")
    public CountryResponse findById(@PathVariable UUID id) {
        return countryService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CountryResponse> create(@Valid @RequestBody CountryRequest request) {
        CountryResponse created = countryService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/countries/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CountryResponse update(@PathVariable UUID id, @Valid @RequestBody CountryRequest request) {
        return countryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        countryService.delete(id);
    }
}
