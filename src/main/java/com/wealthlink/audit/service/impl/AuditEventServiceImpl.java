package com.wealthlink.audit.service.impl;

import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;
import com.wealthlink.audit.entity.AuditEvent;
import com.wealthlink.audit.repository.AuditEventRepository;
import com.wealthlink.audit.service.AuditEventService;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventServiceImpl implements AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public AuditEventResponse recordEvent(CreateAuditRecordRequest request) {
        log.debug("Recording audit event: action={}, entityType={}, entityId={}",
                request.getAction(), request.getEntityType(), request.getEntityId());

        AppUser user = null;
        if (request.getUserId() != null) {
            user = appUserRepository.findById(request.getUserId()).orElse(null);
        }

        AuditEvent event = AuditEvent.builder()
                .user(user)
                .action(request.getAction())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .oldValue(request.getOldValue())
                .newValue(request.getNewValue())
                .correlationId(request.getCorrelationId())
                .ipAddress(request.getIpAddress())
                .requestId(request.getRequestId())
                .build();

        AuditEvent saved = auditEventRepository.save(event);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditTrailForEntity(String entityType, UUID entityId) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditTrailByCorrelationId(String correlationId) {
        return auditEventRepository.findByCorrelationId(correlationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditTrailByUser(UUID userId) {
        return auditEventRepository.findByUserIdOrderByOccurredAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditEventResponse mapToResponse(AuditEvent event) {
        return AuditEventResponse.builder()
                .id(event.getId())
                .userId(event.getUser() != null ? event.getUser().getId() : null)
                .username(event.getUser() != null ? event.getUser().getUsername() : "SYSTEM")
                .action(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .oldValue(event.getOldValue())
                .newValue(event.getNewValue())
                .correlationId(event.getCorrelationId())
                .ipAddress(event.getIpAddress())
                .requestId(event.getRequestId())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}