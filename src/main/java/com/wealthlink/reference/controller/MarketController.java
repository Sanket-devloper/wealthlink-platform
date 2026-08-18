package com.wealthlink.reference.controller;

import com.wealthlink.reference.dto.MarketRequest;
import com.wealthlink.reference.dto.MarketResponse;
import com.wealthlink.reference.service.MarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/markets")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping
    public List<MarketResponse> findAll(@RequestParam(required = false) UUID countryId) {
        return countryId != null ? marketService.findByCountry(countryId) : marketService.findAll();
    }

    @GetMapping("/{id}")
    public MarketResponse findById(@PathVariable UUID id) {
        return marketService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MarketResponse> create(@Valid @RequestBody MarketRequest request) {
        MarketResponse created = marketService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/markets/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MarketResponse update(@PathVariable UUID id, @Valid @RequestBody MarketRequest request) {
        return marketService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        marketService.delete(id);
    }
}
