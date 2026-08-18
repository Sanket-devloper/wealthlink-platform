package com.wealthlink.portfolio.entity;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "position", uniqueConstraints = {
        @UniqueConstraint(name = "uq_position_portfolio_fund_date", columnNames = {"portfolio_id", "fund_share_class_id", "position_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Position {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_portfolio"))
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_share_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_fund_share_class"))
    private FundShareClass fundShareClass;

    @Column(name = "position_date", nullable = false)
    private LocalDate positionDate;

    @Column(name = "quantity", nullable = false, precision = 24, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false, precision = 24, scale = 8)
    private BigDecimal averageCost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cost_basis_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_cost_basis_currency"))
    private Currency costBasisCurrency;

    @Column(name = "market_value", nullable = false, precision = 24, scale = 6)
    private BigDecimal marketValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_currency"))
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PositionStatus status;

    @Column(name = "computed_at", nullable = false, updatable = false)
    private Instant computedAt;

    @PrePersist
    void onCreate() {
        if (this.computedAt == null) {
            this.computedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = PositionStatus.OPEN;
        }
    }
}
