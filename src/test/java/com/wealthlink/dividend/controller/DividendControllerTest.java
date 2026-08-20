package com.wealthlink.dividend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.dividend.dto.request.DeclareDividendRequest;
import com.wealthlink.dividend.dto.response.DividendAllocationResponse;
import com.wealthlink.dividend.dto.response.DividendEventResponse;
import com.wealthlink.dividend.entity.DividendAllocationStatus;
import com.wealthlink.dividend.entity.DividendEventStatus;
import com.wealthlink.dividend.service.DividendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DividendController.class)
class DividendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DividendService dividendService;

    @Test
    @DisplayName("POST /api/v1/dividends/events returns 201 CREATED")
    void declareDividend_Returns201() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID shareClassId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();

        DeclareDividendRequest request = DeclareDividendRequest.builder()
                .fundShareClassId(shareClassId)
                .currencyId(currencyId)
                .exDate(LocalDate.of(2026, 8, 25))
                .recordDate(LocalDate.of(2026, 8, 26))
                .paymentDate(LocalDate.of(2026, 9, 1))
                .dividendPerUnit(new BigDecimal("1.25000000"))
                .source("MANUAL")
                .build();

        DividendEventResponse response = DividendEventResponse.builder()
                .id(eventId)
                .fundShareClassId(shareClassId)
                .currencyId(currencyId)
                .currencyCode("EUR")
                .exDate(request.getExDate())
                .recordDate(request.getRecordDate())
                .paymentDate(request.getPaymentDate())
                .dividendPerUnit(request.getDividendPerUnit())
                .status(DividendEventStatus.DECLARED)
                .source(request.getSource())
                .createdAt(Instant.now())
                .build();

        when(dividendService.declareDividend(any(DeclareDividendRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/dividends/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.status").value("DECLARED"))
                .andExpect(jsonPath("$.currencyCode").value("EUR"))
                .andExpect(jsonPath("$.dividendPerUnit").value(1.25));
    }

    @Test
    @DisplayName("POST /api/v1/dividends/events returns 400 BAD REQUEST when validation fails")
    void declareDividend_ValidationFailure_Returns400() throws Exception {
        // Missing required fields (e.g. fundShareClassId is null)
        DeclareDividendRequest invalidRequest = DeclareDividendRequest.builder()
                .dividendPerUnit(new BigDecimal("-1.00")) // negative amount
                .build();

        mockMvc.perform(post("/api/v1/dividends/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/dividends/events/{eventId} returns 200 OK")
    void getEventById_Returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        DividendEventResponse response = DividendEventResponse.builder()
                .id(eventId)
                .status(DividendEventStatus.DECLARED)
                .dividendPerUnit(new BigDecimal("1.25000000"))
                .build();

        when(dividendService.getDividendEventById(eventId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dividends/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.status").value("DECLARED"));
    }

    @Test
    @DisplayName("POST /api/v1/dividends/events/{eventId}/calculate returns 200 OK")
    void calculateAllocations_Returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        DividendEventResponse response = DividendEventResponse.builder()
                .id(eventId)
                .status(DividendEventStatus.PROCESSING)
                .build();

        when(dividendService.calculateAllocations(eventId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/dividends/events/{eventId}/calculate", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("GET /api/v1/dividends/events/{eventId}/allocations returns 200 OK with list")
    void getAllocationsByEvent_Returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID allocId = UUID.randomUUID();

        DividendAllocationResponse allocation = DividendAllocationResponse.builder()
                .id(allocId)
                .dividendEventId(eventId)
                .positionQuantity(new BigDecimal("500.00000000"))
                .grossAmount(new BigDecimal("625.000000"))
                .taxAmount(new BigDecimal("93.750000"))
                .netAmount(new BigDecimal("531.250000"))
                .currencyCode("EUR")
                .status(DividendAllocationStatus.ALLOCATED)
                .build();

        when(dividendService.getAllocationsByEvent(eventId)).thenReturn(List.of(allocation));

        mockMvc.perform(get("/api/v1/dividends/events/{eventId}/allocations", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(allocId.toString()))
                .andExpect(jsonPath("$[0].grossAmount").value(625.0))
                .andExpect(jsonPath("$[0].netAmount").value(531.25));
    }
}