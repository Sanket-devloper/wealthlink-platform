package com.wealthlink.dividend.service.impl;

import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.service.AuditEventService;
import com.wealthlink.dividend.dto.request.DeclareDividendRequest;
import com.wealthlink.dividend.dto.response.DividendAllocationResponse;
import com.wealthlink.dividend.dto.response.DividendEventResponse;
import com.wealthlink.dividend.entity.DividendAllocation;
import com.wealthlink.dividend.entity.DividendAllocationStatus;
import com.wealthlink.dividend.entity.DividendEvent;
import com.wealthlink.dividend.entity.DividendEventStatus;
import com.wealthlink.dividend.repository.DividendAllocationRepository;
import com.wealthlink.dividend.repository.DividendEventRepository;
import com.wealthlink.dividend.service.DividendService;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.entity.Position;
import com.wealthlink.portfolio.repository.PositionRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DividendServiceImpl implements DividendService {

    private final DividendEventRepository eventRepository;
    private final DividendAllocationRepository allocationRepository;
    private final FundShareClassRepository fundShareClassRepository;
    private final CurrencyRepository currencyRepository;
    private final PositionRepository positionRepository;
    private final AuditEventService auditEventService;

    private static final BigDecimal DEFAULT_WITHHOLDING_TAX_RATE = new BigDecimal("0.15"); // 15% standard WHT

    @Override
    @Transactional
    public DividendEventResponse declareDividend(DeclareDividendRequest request) {
        log.info("Declaring dividend event for fund share class: {}", request.getFundShareClassId());

        if (request.getExDate().isAfter(request.getRecordDate())) {
            throw new IllegalArgumentException("Ex-dividend date cannot be after record date.");
        }
        if (request.getRecordDate().isAfter(request.getPaymentDate())) {
            throw new IllegalArgumentException("Record date cannot be after payment date.");
        }

        FundShareClass fundShareClass = fundShareClassRepository.findById(request.getFundShareClassId())
                .orElseThrow(() -> new EntityNotFoundException("FundShareClass not found: " + request.getFundShareClassId()));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new EntityNotFoundException("Currency not found: " + request.getCurrencyId()));

        DividendEvent correctedFrom = null;
        if (request.getCorrectedFromEventId() != null) {
            correctedFrom = eventRepository.findById(request.getCorrectedFromEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Corrected event not found: " + request.getCorrectedFromEventId()));
        }

        DividendEvent event = DividendEvent.builder()
                .fundShareClass(fundShareClass)
                .currency(currency)
                .exDate(request.getExDate())
                .recordDate(request.getRecordDate())
                .paymentDate(request.getPaymentDate())
                .dividendPerUnit(request.getDividendPerUnit())
                .source(request.getSource())
                .correctedFromEvent(correctedFrom)
                .status(DividendEventStatus.DECLARED)
                .build();

        DividendEvent savedEvent = eventRepository.save(event);

        // Record Audit Trail
        auditEventService.recordEvent(CreateAuditRecordRequest.builder()
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(savedEvent.getId())
                .newValue("{\"dividendPerUnit\":" + savedEvent.getDividendPerUnit() + ",\"currency\":\"" + currency.getIsoCode() + "\"}")
                .correlationId("DIV-DECL-" + savedEvent.getId())
                .build());

        return mapToEventResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public DividendEventResponse getDividendEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapToEventResponse)
                .orElseThrow(() -> new EntityNotFoundException("Dividend event not found: " + eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DividendEventResponse> getEventsByShareClass(UUID shareClassId) {
        return eventRepository.findByFundShareClassId(shareClassId).stream()
                .map(this::mapToEventResponse)
                .toList();
    }

    @Override
    @Transactional
    public DividendEventResponse calculateAllocations(UUID eventId) {
        DividendEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Dividend event not found: " + eventId));

        if (event.getStatus() != DividendEventStatus.DECLARED) {
            throw new IllegalStateException("Allocations can only be calculated when status is DECLARED.");
        }

        // 1. Fetch positions holding units as of the Dividend Record Date (Dev 3 Position module)
        List<Position> positions = positionRepository.findByFundShareClassIdAndPositionDate(
                event.getFundShareClass().getId(),
                event.getRecordDate()
        );

        List<DividendAllocation> allocations = new ArrayList<>();

        // 2. Calculate gross, tax, and net amount per investor portfolio
        for (Position position : positions) {
            if (position.getQuantity() != null && position.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal holdingQty = position.getQuantity();
                BigDecimal grossAmount = holdingQty.multiply(event.getDividendPerUnit()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal taxAmount = grossAmount.multiply(DEFAULT_WITHHOLDING_TAX_RATE).setScale(6, RoundingMode.HALF_UP);
                BigDecimal netAmount = grossAmount.subtract(taxAmount);

                DividendAllocation allocation = DividendAllocation.builder()
                        .dividendEvent(event)
                        .portfolio(position.getPortfolio())
                        .positionQuantity(holdingQty)
                        .grossAmount(grossAmount)
                        .taxAmount(taxAmount)
                        .netAmount(netAmount)
                        .currency(event.getCurrency())
                        .status(DividendAllocationStatus.ALLOCATED)
                        .build();

                allocations.add(allocation);
            }
        }

        if (!allocations.isEmpty()) {
            allocationRepository.saveAll(allocations);
        }

        // 3. Transition event status to PROCESSING
        event.setStatus(DividendEventStatus.PROCESSING);
        DividendEvent updatedEvent = eventRepository.save(event);

        // Record Audit Trail
        auditEventService.recordEvent(CreateAuditRecordRequest.builder()
                .action("DIVIDEND_ALLOCATIONS_CALCULATED")
                .entityType("DividendEvent")
                .entityId(updatedEvent.getId())
                .newValue("{\"totalAllocations\":" + allocations.size() + "}")
                .correlationId("DIV-CALC-" + updatedEvent.getId())
                .build());

        return mapToEventResponse(updatedEvent);
    }

    @Override
    @Transactional
    public DividendEventResponse approveDividend(UUID eventId) {
        DividendEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Dividend event not found: " + eventId));

        if (event.getStatus() != DividendEventStatus.PROCESSING) {
            throw new IllegalStateException("Event must be in PROCESSING status before approval.");
        }

        auditEventService.recordEvent(CreateAuditRecordRequest.builder()
                .action("DIVIDEND_APPROVED")
                .entityType("DividendEvent")
                .entityId(event.getId())
                .newValue("{\"status\":\"APPROVED\"}")
                .correlationId("DIV-APPR-" + event.getId())
                .build());

        return mapToEventResponse(event);
    }

    @Override
    @Transactional
    public DividendEventResponse payDividend(UUID eventId) {
        DividendEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Dividend event not found: " + eventId));

        if (event.getStatus() != DividendEventStatus.PROCESSING) {
            throw new IllegalStateException("Event must be in PROCESSING status before payment execution.");
        }

        List<DividendAllocation> allocations = allocationRepository.findByDividendEventId(eventId);

        for (DividendAllocation allocation : allocations) {
            UUID journalId = UUID.randomUUID(); // Ledger posting reference
            allocation.setJournalId(journalId);
            allocation.setStatus(DividendAllocationStatus.ALLOCATED);
        }

        if (!allocations.isEmpty()) {
            allocationRepository.saveAll(allocations);
        }

        event.setStatus(DividendEventStatus.COMPLETED);
        DividendEvent completedEvent = eventRepository.save(event);

        auditEventService.recordEvent(CreateAuditRecordRequest.builder()
                .action("DIVIDEND_PAYMENT_COMPLETED")
                .entityType("DividendEvent")
                .entityId(completedEvent.getId())
                .newValue("{\"allocationsPaid\":" + allocations.size() + "}")
                .correlationId("DIV-PAY-" + completedEvent.getId())
                .build());

        return mapToEventResponse(completedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DividendAllocationResponse> getAllocationsByEvent(UUID eventId) {
        return allocationRepository.findByDividendEventId(eventId).stream()
                .map(this::mapToAllocationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DividendAllocationResponse> getAllocationsByPortfolio(UUID portfolioId) {
        return allocationRepository.findByPortfolioId(portfolioId).stream()
                .map(this::mapToAllocationResponse)
                .toList();
    }

    private DividendEventResponse mapToEventResponse(DividendEvent event) {
        return DividendEventResponse.builder()
                .id(event.getId())
                .fundShareClassId(event.getFundShareClass() != null ? event.getFundShareClass().getId() : null)
                .currencyId(event.getCurrency() != null ? event.getCurrency().getId() : null)
                .currencyCode(event.getCurrency() != null ? event.getCurrency().getIsoCode() : null)
                .exDate(event.getExDate())
                .recordDate(event.getRecordDate())
                .paymentDate(event.getPaymentDate())
                .dividendPerUnit(event.getDividendPerUnit())
                .status(event.getStatus())
                .source(event.getSource())
                .correctedFromEventId(event.getCorrectedFromEvent() != null ? event.getCorrectedFromEvent().getId() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }

    private DividendAllocationResponse mapToAllocationResponse(DividendAllocation alloc) {
        return DividendAllocationResponse.builder()
                .id(alloc.getId())
                .dividendEventId(alloc.getDividendEvent() != null ? alloc.getDividendEvent().getId() : null)
                .portfolioId(alloc.getPortfolio() != null ? alloc.getPortfolio().getId() : null)
                .positionQuantity(alloc.getPositionQuantity())
                .grossAmount(alloc.getGrossAmount())
                .taxAmount(alloc.getTaxAmount())
                .netAmount(alloc.getNetAmount())
                .currencyCode(alloc.getCurrency() != null ? alloc.getCurrency().getIsoCode() : null)
                .status(alloc.getStatus())
                .journalId(alloc.getJournalId())
                .createdAt(alloc.getCreatedAt())
                .build();
    }
}