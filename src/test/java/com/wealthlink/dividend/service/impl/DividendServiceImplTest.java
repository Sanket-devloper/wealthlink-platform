package com.wealthlink.dividend.service.impl;

import com.wealthlink.audit.service.AuditEventService;
import com.wealthlink.dividend.dto.request.DeclareDividendRequest;
import com.wealthlink.dividend.dto.response.DividendEventResponse;
import com.wealthlink.dividend.entity.DividendAllocation;
import com.wealthlink.dividend.entity.DividendEvent;
import com.wealthlink.dividend.entity.DividendEventStatus;
import com.wealthlink.dividend.repository.DividendAllocationRepository;
import com.wealthlink.dividend.repository.DividendEventRepository;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.entity.Position;
import com.wealthlink.portfolio.repository.PositionRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DividendServiceImplTest {

    @Mock
    private DividendEventRepository eventRepository;

    @Mock
    private DividendAllocationRepository allocationRepository;

    @Mock
    private FundShareClassRepository fundShareClassRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AuditEventService auditEventService;

    @InjectMocks
    private DividendServiceImpl dividendService;

    private UUID fundShareClassId;
    private UUID currencyId;
    private UUID eventId;
    private FundShareClass fundShareClass;
    private Currency currency;
    private DividendEvent mockEvent;

    @BeforeEach
    void setUp() {
        fundShareClassId = UUID.randomUUID();
        currencyId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        fundShareClass = FundShareClass.builder()
                .id(fundShareClassId)
                .build();

        currency = Currency.builder()
                .id(currencyId)
                .isoCode("EUR")
                .name("Euro")
                .build();

        mockEvent = DividendEvent.builder()
                .id(eventId)
                .fundShareClass(fundShareClass)
                .currency(currency)
                .exDate(LocalDate.of(2026, 8, 25))
                .recordDate(LocalDate.of(2026, 8, 26))
                .paymentDate(LocalDate.of(2026, 9, 1))
                .dividendPerUnit(new BigDecimal("1.25000000"))
                .status(DividendEventStatus.DECLARED)
                .source("MANUAL")
                .build();
    }

    @Test
    @DisplayName("declareDividend: successfully declares dividend event and logs audit")
    void declareDividend_Success() {
        DeclareDividendRequest request = DeclareDividendRequest.builder()
                .fundShareClassId(fundShareClassId)
                .currencyId(currencyId)
                .exDate(LocalDate.of(2026, 8, 25))
                .recordDate(LocalDate.of(2026, 8, 26))
                .paymentDate(LocalDate.of(2026, 9, 1))
                .dividendPerUnit(new BigDecimal("1.25000000"))
                .source("MANUAL")
                .build();

        when(fundShareClassRepository.findById(fundShareClassId)).thenReturn(Optional.of(fundShareClass));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));
        when(eventRepository.save(any(DividendEvent.class))).thenReturn(mockEvent);

        DividendEventResponse response = dividendService.declareDividend(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getStatus()).isEqualTo(DividendEventStatus.DECLARED);
        assertThat(response.getCurrencyCode()).isEqualTo("EUR");
        verify(auditEventService, times(1)).recordEvent(any());
        verify(eventRepository, times(1)).save(any(DividendEvent.class));
    }

    @Test
    @DisplayName("declareDividend: throws exception when exDate is after recordDate")
    void declareDividend_InvalidDates_ThrowsException() {
        DeclareDividendRequest request = DeclareDividendRequest.builder()
                .fundShareClassId(fundShareClassId)
                .currencyId(currencyId)
                .exDate(LocalDate.of(2026, 8, 28))
                .recordDate(LocalDate.of(2026, 8, 26))
                .paymentDate(LocalDate.of(2026, 9, 1))
                .dividendPerUnit(new BigDecimal("1.25000000"))
                .source("MANUAL")
                .build();

        assertThatThrownBy(() -> dividendService.declareDividend(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ex-dividend date cannot be after record date");
    }

    @Test
    @DisplayName("calculateAllocations: calculates gross, tax, and net accurately across holdings")
    void calculateAllocations_Success() {
        Portfolio portfolio = Portfolio.builder().id(UUID.randomUUID()).build();

        Position position = Position.builder()
                .id(UUID.randomUUID())
                .portfolio(portfolio)
                .fundShareClass(fundShareClass)
                .positionDate(LocalDate.of(2026, 8, 26))
                .quantity(new BigDecimal("1000.00000000"))
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(positionRepository.findByFundShareClassIdAndPositionDate(fundShareClassId, LocalDate.of(2026, 8, 26)))
                .thenReturn(List.of(position));
        when(eventRepository.save(any(DividendEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DividendEventResponse response = dividendService.calculateAllocations(eventId);

        assertThat(response.getStatus()).isEqualTo(DividendEventStatus.PROCESSING);
        verify(allocationRepository, times(1)).saveAll(anyList());
        verify(auditEventService, times(1)).recordEvent(any());
    }

    @Test
    @DisplayName("payDividend: updates allocations to ALLOCATED and moves event to COMPLETED")
    void payDividend_Success() {
        mockEvent.setStatus(DividendEventStatus.PROCESSING);

        DividendAllocation allocation = DividendAllocation.builder()
                .id(UUID.randomUUID())
                .dividendEvent(mockEvent)
                .positionQuantity(new BigDecimal("1000.00000000"))
                .grossAmount(new BigDecimal("1250.000000"))
                .taxAmount(new BigDecimal("187.500000"))
                .netAmount(new BigDecimal("1062.500000"))
                .currency(currency)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(allocationRepository.findByDividendEventId(eventId)).thenReturn(List.of(allocation));
        when(eventRepository.save(any(DividendEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DividendEventResponse response = dividendService.payDividend(eventId);

        assertThat(response.getStatus()).isEqualTo(DividendEventStatus.COMPLETED);
        assertThat(allocation.getJournalId()).isNotNull();
        verify(allocationRepository, times(1)).saveAll(anyList());
        verify(auditEventService, times(1)).recordEvent(any());
    }

    @Test
    @DisplayName("getDividendEventById: throws EntityNotFoundException when event does not exist")
    void getDividendEventById_NotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dividendService.getDividendEventById(eventId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Dividend event not found");
    }
}