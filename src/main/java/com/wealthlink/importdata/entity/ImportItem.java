package com.wealthlink.importdata.entity;

import com.wealthlink.marketdata.entity.FundPrice;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A single row's result within an IMPORT_BATCH. The transaction boundary is
 * per IMPORT_ITEM -> FUND_PRICE, not the whole batch (architecture doc,
 * Section 11 "Import flow"), so one bad row must not roll back the
 * thousands of valid rows around it. raw_payload keeps the original
 * provider record for replay/debugging; error_details is populated only on
 * failure.
 */
@Entity
@Table(name = "import_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ImportItem {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_import_item_batch"))
    private ImportBatch importBatch;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "json", nullable = false)
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImportItemStatus status;

    @Column(name = "error_details")
    private String errorDetails;

    /** Set once the item has been successfully persisted as a FUND_PRICE row. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "fund_price_id", nullable = true, foreignKey = @ForeignKey(name = "fk_import_item_fund_price"))
    private FundPrice fundPrice;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    void onCreate() {
        if (this.status == null) {
            this.status = ImportItemStatus.PENDING;
        }
    }
}
