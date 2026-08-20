package com.wealthlink.audit.service;

import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;

import java.util.List;
import java.util.UUID;

public interface AuditEventService {
    AuditEventResponse recordEvent(CreateAuditRecordRequest request);
    List<AuditEventResponse> getAuditTrailForEntity(String entityType, UUID entityId);
    List<AuditEventResponse> getAuditTrailByCorrelationId(String correlationId);
    List<AuditEventResponse> getAuditTrailByUser(UUID userId);
}