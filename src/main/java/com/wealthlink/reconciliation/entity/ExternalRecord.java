package com.wealthlink.reconciliation.entity;

import com.wealthlink.fund.entity.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "external_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ExternalRecord {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reconciliation_run_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ext_record_run"))
    private ReconciliationRun reconciliationRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ext_record_provider"))
    private Provider provider;

    @Column(name = "external_reference", nullable = false, length = 255)
    private String externalReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    void onCreate() {
        if (this.receivedAt == null) {
            this.receivedAt = Instant.now();
        }
    }
}