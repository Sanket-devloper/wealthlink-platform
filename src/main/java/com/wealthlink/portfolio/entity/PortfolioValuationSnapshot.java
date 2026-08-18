package com.wealthlink.portfolio.entity;

import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "portfolio_valuation_snapshot", uniqueConstraints = {
        @UniqueConstraint(name = "uq_portfolio_valuation_date", columnNames = {"portfolio_id", "valuation_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class PortfolioValuationSnapshot {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_valuation_portfolio"))
    private Portfolio portfolio;

    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;

    @Column(name = "total_value", nullable = false, precision = 24, scale = 6)
    private BigDecimal totalValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_valuation_currency"))
    private Currency currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
