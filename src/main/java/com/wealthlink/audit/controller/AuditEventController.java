package com.wealthlink.audit.controller;

import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;
import com.wealthlink.audit.service.AuditEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audits")
@RequiredArgsConstructor
public class AuditEventController {

    private final AuditEventService auditEventService;

    @PostMapping
    public ResponseEntity<AuditEventResponse> recordAuditEvent(@Valid @RequestBody CreateAuditRecordRequest request) {
        return new ResponseEntity<>(auditEventService.recordEvent(request), HttpStatus.CREATED);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditEventResponse>> getEntityAuditTrail(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        return ResponseEntity.ok(auditEventService.getAuditTrailForEntity(entityType, entityId));
    }

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<AuditEventResponse>> getByCorrelationId(@PathVariable String correlationId) {
        return ResponseEntity.ok(auditEventService.getAuditTrailByCorrelationId(correlationId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditEventResponse>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(auditEventService.getAuditTrailByUser(userId));
    }
}