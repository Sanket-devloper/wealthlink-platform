package com.wealthlink.trade.entity;

import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trade_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class TradeExecution {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trade_execution_trade_order"))
    private TradeOrder tradeOrder;

    @Column(name = "execution_reference", nullable = false, unique = true, length = 50)
    private String executionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeExecutionStatus status;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "executed_quantity", nullable = false, precision = 24, scale = 8)
    private BigDecimal executedQuantity;

    @Column(name = "execution_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal executionPrice;

    @Column(name = "gross_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal grossAmount;

    @Column(name = "fee", nullable = false, precision = 24, scale = 6)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "tax", nullable = false, precision = 24, scale = 6)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 24, scale = 6)
    private BigDecimal netAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_trade_execution_currency"))
    private Currency currency;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = TradeExecutionStatus.PENDING;
        }
        if (this.fee == null) {
            this.fee = BigDecimal.ZERO;
        }
        if (this.tax == null) {
            this.tax = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
