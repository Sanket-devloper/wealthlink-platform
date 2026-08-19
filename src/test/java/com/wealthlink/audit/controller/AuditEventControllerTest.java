package com.wealthlink.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;
import com.wealthlink.audit.service.AuditEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditEventController.class)
class AuditEventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuditEventService auditEventService;

    @Test
    void recordAuditEventReturnsCreated() throws Exception {
        UUID entityId = UUID.randomUUID();
        CreateAuditRecordRequest request = CreateAuditRecordRequest.builder()
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .build();

        AuditEventResponse response = AuditEventResponse.builder()
                .id(UUID.randomUUID())
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .occurredAt(Instant.now())
                .build();

        given(auditEventService.recordEvent(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/audits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("DIVIDEND_DECLARED"))
                .andExpect(jsonPath("$.entityType").value("DividendEvent"));
    }
}