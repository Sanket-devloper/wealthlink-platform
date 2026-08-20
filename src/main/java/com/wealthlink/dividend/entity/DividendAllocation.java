package com.wealthlink.dividend.entity;

import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dividend_allocation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_div_alloc_event_portfolio", columnNames = {"dividend_event_id", "portfolio_id"})
}, indexes = {
        @Index(name = "idx_div_alloc_portfolio", columnList = "portfolio_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")   //Generate equals() and hashCode() methods using only the id field.
public class DividendAllocation {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dividend_event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_div_alloc_event"))
    private DividendEvent dividendEvent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_div_alloc_portfolio"))
    private Portfolio portfolio;

    @Column(name = "position_quantity", precision = 24, scale = 8, nullable = false)
    private BigDecimal positionQuantity;

    @Column(name = "gross_amount", precision = 24, scale = 6, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "tax_amount", precision = 24, scale = 6, nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "net_amount", precision = 24, scale = 6, nullable = false)
    private BigDecimal netAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_div_alloc_currency"))
    private Currency currency;

    @Column(name = "journal_id")
    private UUID journalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DividendAllocationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = DividendAllocationStatus.PENDING;
        }
        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }
    }
}