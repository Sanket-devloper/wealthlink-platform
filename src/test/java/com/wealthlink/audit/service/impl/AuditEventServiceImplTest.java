package com.wealthlink.audit.service.impl;

import com.wealthlink.audit.dto.request.CreateAuditRecordRequest;
import com.wealthlink.audit.dto.response.AuditEventResponse;
import com.wealthlink.audit.entity.AuditEvent;
import com.wealthlink.audit.repository.AuditEventRepository;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceImplTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AuditEventServiceImpl auditEventService;

    private UUID eventId;
    private UUID userId;
    private UUID entityId;
    private AppUser mockUser;
    private AuditEvent mockEventWithUser;
    private AuditEvent mockSystemEvent;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();
        entityId = UUID.randomUUID();

        mockUser = AppUser.builder()
                .id(userId)
                .username("auditor_admin")
                .build();

        mockEventWithUser = AuditEvent.builder()
                .id(eventId)
                .user(mockUser)
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .oldValue(null)
                .newValue("{\"dividendPerUnit\": 1.25}")
                .correlationId("CORR-9901")
                .ipAddress("192.168.1.100")
                .requestId("REQ-7788")
                .occurredAt(Instant.now())
                .build();

        mockSystemEvent = AuditEvent.builder()
                .id(eventId)
                .user(null)
                .action("RECONCILIATION_AUTO_RUN")
                .entityType("ReconciliationRun")
                .entityId(entityId)
                .oldValue(null)
                .newValue("{\"status\": \"IN_PROGRESS\"}")
                .correlationId("CORR-SYSTEM-1")
                .ipAddress("127.0.0.1")
                .requestId("REQ-SYS-1")
                .occurredAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("recordEvent: successfully records audit event with user context")
    void recordEvent_WithUser_Success() {
        CreateAuditRecordRequest request = CreateAuditRecordRequest.builder()
                .userId(userId)
                .action("DIVIDEND_DECLARED")
                .entityType("DividendEvent")
                .entityId(entityId)
                .newValue("{\"dividendPerUnit\": 1.25}")
                .correlationId("CORR-9901")
                .ipAddress("192.168.1.100")
                .requestId("REQ-7788")
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(mockEventWithUser);

        AuditEventResponse response = auditEventService.recordEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("auditor_admin");
        assertThat(response.getAction()).isEqualTo("DIVIDEND_DECLARED");
        assertThat(response.getEntityType()).isEqualTo("DividendEvent");
        assertThat(response.getCorrelationId()).isEqualTo("CORR-9901");
        verify(appUserRepository, times(1)).findById(userId);
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("recordEvent: defaults username to 'SYSTEM' when userId is null")
    void recordEvent_WithoutUser_DefaultsToSystem() {
        CreateAuditRecordRequest request = CreateAuditRecordRequest.builder()
                .userId(null)
                .action("RECONCILIATION_AUTO_RUN")
                .entityType("ReconciliationRun")
                .entityId(entityId)
                .newValue("{\"status\": \"IN_PROGRESS\"}")
                .correlationId("CORR-SYSTEM-1")
                .ipAddress("127.0.0.1")
                .requestId("REQ-SYS-1")
                .build();

        when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(mockSystemEvent);

        AuditEventResponse response = auditEventService.recordEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getUsername()).isEqualTo("SYSTEM");
        assertThat(response.getAction()).isEqualTo("RECONCILIATION_AUTO_RUN");
        verify(appUserRepository, never()).findById(any());
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("getAuditTrailForEntity: returns audit logs sorted by occurredAt descending")
    void getAuditTrailForEntity_Success() {
        when(auditEventRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc("DividendEvent", entityId))
                .thenReturn(List.of(mockEventWithUser));

        List<AuditEventResponse> responses = auditEventService.getAuditTrailForEntity("DividendEvent", entityId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEntityType()).isEqualTo("DividendEvent");
        assertThat(responses.get(0).getEntityId()).isEqualTo(entityId);
        verify(auditEventRepository, times(1))
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc("DividendEvent", entityId);
    }

    @Test
    @DisplayName("getAuditTrailByCorrelationId: returns audit logs matching correlationId")
    void getAuditTrailByCorrelationId_Success() {
        when(auditEventRepository.findByCorrelationId("CORR-9901"))
                .thenReturn(List.of(mockEventWithUser));

        List<AuditEventResponse> responses = auditEventService.getAuditTrailByCorrelationId("CORR-9901");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCorrelationId()).isEqualTo("CORR-9901");
        verify(auditEventRepository, times(1)).findByCorrelationId("CORR-9901");
    }

    @Test
    @DisplayName("getAuditTrailByUser: returns audit logs for specific user ordered descending")
    void getAuditTrailByUser_Success() {
        when(auditEventRepository.findByUserIdOrderByOccurredAtDesc(userId))
                .thenReturn(List.of(mockEventWithUser));

        List<AuditEventResponse> responses = auditEventService.getAuditTrailByUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(userId);
        assertThat(responses.get(0).getUsername()).isEqualTo("auditor_admin");
        verify(auditEventRepository, times(1)).findByUserIdOrderByOccurredAtDesc(userId);
    }
}