package com.wealthlink.importdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A single execution run of an IMPORT_JOB. idempotency_key protects safe
 * retry (Required Cross-Module Test 3: "Run IMPORT_BATCH with key X, retry
 * with key X -> no duplicate completed batch").
 * <p>
 * Carries {@code @Version} for optimistic locking per the architecture doc
 * Section 32 (IMPORT_BATCH is one of the explicitly-listed candidates,
 * since success/failure counters are mutated concurrently as items land).
 */
@Entity
@Table(name = "import_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ImportBatch {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_import_batch_job"))
    private ImportJob importJob;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImportBatchStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder.Default
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @Builder.Default
    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;

    @Builder.Default
    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = ImportBatchStatus.PENDING;
        }
    }

    public void incrementTotalItems() {
        this.totalItems++;
    }

    public void incrementSuccessCount() {
        this.successCount++;
    }

    public void incrementFailureCount() {
        this.failureCount++;
    }
}
