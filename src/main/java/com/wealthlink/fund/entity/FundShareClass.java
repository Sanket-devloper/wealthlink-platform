package com.wealthlink.fund.entity;

import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * The tradable / priced share class of a fund. A fund can have multiple
 * share classes with different currencies and fee structures - this is the
 * entity that FUND_PRICE, FUND_PROVIDER_MAPPING, and (downstream, Dev 3)
 * PORTFOLIO/POSITION actually reference, not FUND itself.
 */
@Entity
@Table(name = "fund_share_class", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fund_share_class_fund_code", columnNames = {"fund_id", "class_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class FundShareClass {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_share_class_fund"))
    private Fund fund;

    @Column(name = "class_code", nullable = false, length = 30)
    private String classCode;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_share_class_currency"))
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShareClassStatus status;

    @PrePersist
    void onCreate() {
        if (this.status == null) {
            this.status = ShareClassStatus.ACTIVE;
        }
    }
}
