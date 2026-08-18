package com.wealthlink.marketdata.entity;

import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Historical, append-only FX rate. Never updated in place - a correction is
 * a new row, per the architecture's "reversal instead of mutation" rule for
 * date-keyed historical data.
 * <p>
 * Business key (Section 22 / 31 of the architecture doc):
 * (base_currency_id, quote_currency_id, rate_date, rate_type, source_id)
 */
@Entity
@Table(name = "fx_rate", uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_fx_rate_business_key",
                columnNames = {"base_currency_id", "quote_currency_id", "rate_date", "rate_type", "source_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class FxRate {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fx_rate_base_currency"))
    private Currency baseCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fx_rate_quote_currency"))
    private Currency quoteCurrency;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private RateType rateType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fx_rate_source"))
    private FxRateSource source;

    @Column(name = "rate", nullable = false, precision = 24, scale = 8)
    private BigDecimal rate;
}
