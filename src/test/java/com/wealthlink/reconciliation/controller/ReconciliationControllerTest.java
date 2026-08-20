package com.wealthlink.reconciliation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.reconciliation.dto.request.IngestExternalRecordRequest;
import com.wealthlink.reconciliation.dto.request.ResolveReconciliationItemRequest;
import com.wealthlink.reconciliation.dto.request.StartReconciliationRunRequest;
import com.wealthlink.reconciliation.dto.response.ReconciliationItemResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationResolutionResponse;
import com.wealthlink.reconciliation.dto.response.ReconciliationRunResponse;
import com.wealthlink.reconciliation.entity.ReconciliationMatchStatus;
import com.wealthlink.reconciliation.entity.ReconciliationRunStatus;
import com.wealthlink.reconciliation.service.ReconciliationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReconciliationController.class)
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReconciliationService reconciliationService;

    @Test
    @DisplayName("POST /api/v1/reconciliation/runs returns 201 CREATED")
    void startRun_Returns201() throws Exception {
        UUID runId = UUID.randomUUID();
        StartReconciliationRunRequest request = StartReconciliationRunRequest.builder()
                .runType("HOLDINGS")
                .businessDate(LocalDate.of(2026, 8, 19))
                .build();

        ReconciliationRunResponse response = ReconciliationRunResponse.builder()
                .id(runId)
                .runType("HOLDINGS")
                .businessDate(LocalDate.of(2026, 8, 19))
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();

        when(reconciliationService.startRun(any(StartReconciliationRunRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reconciliation/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(runId.toString()))
                .andExpect(jsonPath("$.runType").value("HOLDINGS"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/v1/reconciliation/runs returns 400 BAD REQUEST when body is invalid")
    void startRun_ValidationFailure_Returns400() throws Exception {
        StartReconciliationRunRequest invalidRequest = StartReconciliationRunRequest.builder()
                .runType("") // Blank string fails @NotBlank
                .businessDate(null) // Null fails @NotNull
                .build();

        mockMvc.perform(post("/api/v1/reconciliation/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/reconciliation/runs/{runId}/complete returns 200 OK")
    void completeRun_Returns200() throws Exception {
        UUID runId = UUID.randomUUID();
        ReconciliationRunResponse response = ReconciliationRunResponse.builder()
                .id(runId)
                .status(ReconciliationRunStatus.COMPLETED)
                .completedAt(Instant.now())
                .build();

        when(reconciliationService.completeRun(runId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/reconciliation/runs/{runId}/complete", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(runId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/v1/reconciliation/runs/{runId} returns 200 OK")
    void getRunById_Returns200() throws Exception {
        UUID runId = UUID.randomUUID();
        ReconciliationRunResponse response = ReconciliationRunResponse.builder()
                .id(runId)
                .runType("CASH")
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .build();

        when(reconciliationService.getRunById(runId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reconciliation/runs/{runId}", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(runId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/reconciliation/runs?date=2026-08-19 returns list")
    void getRunsByDate_Returns200() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 19);
        ReconciliationRunResponse response = ReconciliationRunResponse.builder()
                .id(UUID.randomUUID())
                .businessDate(date)
                .build();

        when(reconciliationService.getRunsByBusinessDate(date)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/reconciliation/runs")
                        .param("date", "2026-08-19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessDate").value("2026-08-19"));
    }

    @Test
    @DisplayName("POST /api/v1/reconciliation/external-records returns 202 ACCEPTED")
    void ingestExternalRecord_Returns202() throws Exception {
        IngestExternalRecordRequest request = IngestExternalRecordRequest.builder()
                .reconciliationRunId(UUID.randomUUID())
                .providerId(UUID.randomUUID())
                .externalReference("EXT-12345")
                .rawPayload("{\"balance\": 5000}")
                .build();

        doNothing().when(reconciliationService).ingestExternalRecord(any(IngestExternalRecordRequest.class));

        mockMvc.perform(post("/api/v1/reconciliation/external-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("GET /api/v1/reconciliation/runs/{runId}/items returns item list")
    void getItems_Returns200() throws Exception {
        UUID runId = UUID.randomUUID();
        ReconciliationItemResponse item = ReconciliationItemResponse.builder()
                .id(UUID.randomUUID())
                .reconciliationRunId(runId)
                .matchStatus(ReconciliationMatchStatus.MATCHED)
                .build();

        when(reconciliationService.getItemsByRunAndStatus(runId, ReconciliationMatchStatus.MATCHED))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/reconciliation/runs/{runId}/items", runId)
                        .param("status", "MATCHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchStatus").value("MATCHED"));
    }

    @Test
    @DisplayName("POST /api/v1/reconciliation/items/{itemId}/resolve returns 200 OK")
    void resolveItem_Returns200() throws Exception {
        UUID itemId = UUID.randomUUID();
        UUID resolverId = UUID.randomUUID();

        ResolveReconciliationItemRequest request = ResolveReconciliationItemRequest.builder()
                .resolvedById(resolverId)
                .resolutionType("FORCE_MATCH")
                .resolutionNotes("Manually verified")
                .build();

        ReconciliationResolutionResponse response = ReconciliationResolutionResponse.builder()
                .id(UUID.randomUUID())
                .reconciliationItemId(itemId)
                .resolvedById(resolverId)
                .resolvedByUsername("ops_admin")
                .resolutionType("FORCE_MATCH")
                .resolutionNotes("Manually verified")
                .resolvedAt(Instant.now())
                .build();

        when(reconciliationService.resolveItem(eq(itemId), any(ResolveReconciliationItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/reconciliation/items/{itemId}/resolve", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconciliationItemId").value(itemId.toString()))
                .andExpect(jsonPath("$.resolutionType").value("FORCE_MATCH"))
                .andExpect(jsonPath("$.resolvedByUsername").value("ops_admin"));
    }
}