package com.wealthlink.dividend.controller;

import com.wealthlink.dividend.dto.request.DeclareDividendRequest;
import com.wealthlink.dividend.dto.response.DividendAllocationResponse;
import com.wealthlink.dividend.dto.response.DividendEventResponse;
import com.wealthlink.dividend.service.DividendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dividends")
@RequiredArgsConstructor
public class DividendController {

    private final DividendService dividendService;

    @PostMapping("/events")
    public ResponseEntity<DividendEventResponse> declareDividend(@Valid @RequestBody DeclareDividendRequest request) {
        return new ResponseEntity<>(dividendService.declareDividend(request), HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<DividendEventResponse> getEventById(@PathVariable UUID eventId) {
        return ResponseEntity.ok(dividendService.getDividendEventById(eventId));
    }

    @GetMapping("/events/share-class/{shareClassId}")
    public ResponseEntity<List<DividendEventResponse>> getEventsByShareClass(@PathVariable UUID shareClassId) {
        return ResponseEntity.ok(dividendService.getEventsByShareClass(shareClassId));
    }

    @PostMapping("/events/{eventId}/calculate")
    public ResponseEntity<DividendEventResponse> calculateAllocations(@PathVariable UUID eventId) {
        return ResponseEntity.ok(dividendService.calculateAllocations(eventId));
    }

    @PostMapping("/events/{eventId}/approve")
    public ResponseEntity<DividendEventResponse> approveDividend(@PathVariable UUID eventId) {
        return ResponseEntity.ok(dividendService.approveDividend(eventId));
    }

    @PostMapping("/events/{eventId}/pay")
    public ResponseEntity<DividendEventResponse> payDividend(@PathVariable UUID eventId) {
        return ResponseEntity.ok(dividendService.payDividend(eventId));
    }

    @GetMapping("/events/{eventId}/allocations")
    public ResponseEntity<List<DividendAllocationResponse>> getAllocationsByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(dividendService.getAllocationsByEvent(eventId));
    }

    @GetMapping("/allocations/portfolio/{portfolioId}")
    public ResponseEntity<List<DividendAllocationResponse>> getAllocationsByPortfolio(@PathVariable UUID portfolioId) {
        return ResponseEntity.ok(dividendService.getAllocationsByPortfolio(portfolioId));
    }
}