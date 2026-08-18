package com.wealthlink.reconciliation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_item", indexes = {
        @Index(name = "idx_recon_item_run_status", columnList = "reconciliation_run_id, match_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ReconciliationItem {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reconciliation_run_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recon_item_run"))
    private ReconciliationRun reconciliationRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_record_id", foreignKey = @ForeignKey(name = "fk_recon_item_ext_record"))
    private ExternalRecord externalRecord;

    @Column(name = "internal_reference_type", length = 100)
    private String internalReferenceType;

    @Column(name = "internal_reference_id")
    private UUID internalReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 50)
    private ReconciliationMatchStatus matchStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "difference_details", columnDefinition = "jsonb")
    private String differenceDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}