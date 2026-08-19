package com.wealthlink.audit.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String action;
    private String entityType;
    private UUID entityId;
    private String oldValue;
    private String newValue;
    private String correlationId;
    private String ipAddress;
    private String requestId;
    private Instant occurredAt;
}