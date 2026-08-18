package com.wealthlink.audit.repository;

import com.wealthlink.audit.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, UUID entityId);
    List<AuditEvent> findByCorrelationId(String correlationId);
    List<AuditEvent> findByUserIdOrderByOccurredAtDesc(UUID userId);
}