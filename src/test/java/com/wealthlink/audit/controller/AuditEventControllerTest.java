package com.wealthlink.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;
import com.wealthlink.audit.service.AuditEventService;
import com.wealthlink.security.jwt.JwtService;
import com.wealthlink.security.user.CustomUserDetailsService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/audits returns 201 CREATED")
    void recordAuditEvent_Returns201() throws Exception {

        UUID eventId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateAuditRecordRequest request = CreateAuditRecordRequest.builder()
                .userId(userId)
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .newValue("{\"rate\": 1.25}")
                .correlationId("CORR-5544")
                .ipAddress("127.0.0.1")
                .requestId("REQ-99")
                .build();

        AuditEventResponse response = AuditEventResponse.builder()
                .id(eventId)
                .userId(userId)
                .username("test_user")
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .newValue("{\"rate\": 1.25}")
                .correlationId("CORR-5544")
                .ipAddress("127.0.0.1")
                .requestId("REQ-99")
                .occurredAt(Instant.now())
                .build();

        when(auditEventService.recordEvent(any(CreateAuditRecordRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/audits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.action").value("DIVIDEND_DECLARED"))
                .andExpect(jsonPath("$.entityType").value("DividendEvent"))
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.correlationId").value("CORR-5544"));
    }

    @Test
    @DisplayName("POST /api/v1/audits returns 400 BAD REQUEST when mandatory fields are missing")
    void recordAuditEvent_ValidationFailure_Returns400() throws Exception {

        CreateAuditRecordRequest invalidRequest = CreateAuditRecordRequest.builder()
                .action("")
                .entityType(null)
                .entityId(null)
                .build();

        mockMvc.perform(post("/api/v1/audits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/audits/entity/{entityType}/{entityId} returns 200 OK with audit list")
    void getEntityAuditTrail_Returns200() throws Exception {

        UUID entityId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        AuditEventResponse response = AuditEventResponse.builder()
                .id(eventId)
                .action("POSITION_UPDATED")
                .entityType("Position")
                .entityId(entityId)
                .username("SYSTEM")
                .occurredAt(Instant.now())
                .build();

        when(auditEventService.getAuditTrailForEntity("Position", entityId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(
                        "/api/v1/audits/entity/{entityType}/{entityId}",
                        "Position",
                        entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].entityType").value("Position"))
                .andExpect(jsonPath("$[0].action").value("POSITION_UPDATED"));
    }

    @Test
    @DisplayName("GET /api/v1/audits/correlation/{correlationId} returns 200 OK")
    void getByCorrelationId_Returns200() throws Exception {

        String correlationId = "CORR-TXN-12345";

        AuditEventResponse response = AuditEventResponse.builder()
                .id(UUID.randomUUID())
                .correlationId(correlationId)
                .action("TRADE_MATCHED")
                .build();

        when(auditEventService.getAuditTrailByCorrelationId(correlationId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(
                        "/api/v1/audits/correlation/{correlationId}",
                        correlationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correlationId").value(correlationId))
                .andExpect(jsonPath("$[0].action").value("TRADE_MATCHED"));
    }

    @Test
    @DisplayName("GET /api/v1/audits/user/{userId} returns 200 OK")
    void getByUser_Returns200() throws Exception {

        UUID userId = UUID.randomUUID();

        AuditEventResponse response = AuditEventResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("analyst_user")
                .action("REPORT_EXPORTED")
                .build();

        when(auditEventService.getAuditTrailByUser(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(
                        "/api/v1/audits/user/{userId}",
                        userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("analyst_user"));
    }
}