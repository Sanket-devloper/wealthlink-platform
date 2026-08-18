package com.wealthlink.marketdata.entity;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.entity.Provider;
import com.wealthlink.importdata.entity.ImportBatch;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Historical fund pricing (NAV/BID/ASK per share class per day per provider).
 * <p>
 * CRITICAL business key (DEV2-D1 acceptance criteria / architecture doc
 * Section 11 &amp; 22): uniqueness is anchored to the internal
 * fund_share_class_id, NOT the provider's external fund identifier, so
 * uniqueness holds even if a provider mapping changes later.
 * <pre>
 * UNIQUE(fund_share_class_id, price_date, price_type, provider_id)
 * </pre>
 * import_batch_id is nullable: a price can be entered manually/out of an
 * import context, but when it does come through the import pipeline it
 * traces back to exactly one IMPORT_BATCH.
 */
@Entity
@Table(name = "fund_price",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_fund_price_business_key",
                        columnNames = {"fund_share_class_id", "price_date", "price_type", "provider_id"})
        },
        indexes = {
                @Index(name = "ix_fund_price_share_class_date", columnList = "fund_share_class_id, price_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class FundPrice {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_share_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_price_share_class"))
    private FundShareClass fundShareClass;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_price_provider"))
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_price_currency"))
    private Currency currency;

    @Column(name = "price", nullable = false, precision = 24, scale = 8)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "import_batch_id", nullable = true, foreignKey = @ForeignKey(name = "fk_fund_price_import_batch"))
    private ImportBatch importBatch;
}
