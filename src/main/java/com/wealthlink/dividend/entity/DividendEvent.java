package com.wealthlink.dividend.entity;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dividend_event", indexes = {
        @Index(name = "idx_div_event_dates", columnList = "ex_date, record_date, payment_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class DividendEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_share_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_div_event_fund_share_class"))
    private FundShareClass fundShareClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_div_event_currency"))
    private Currency currency;

    @Column(name = "ex_date", nullable = false)
    private LocalDate exDate;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "dividend_per_unit", precision = 18, scale = 8, nullable = false)
    private BigDecimal dividendPerUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DividendEventStatus status;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_from_event_id", foreignKey = @ForeignKey(name = "fk_div_event_corrected"))
    private DividendEvent correctedFromEvent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = DividendEventStatus.DECLARED;
        }
    }
}