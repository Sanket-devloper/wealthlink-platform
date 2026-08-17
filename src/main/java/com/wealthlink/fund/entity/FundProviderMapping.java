package com.wealthlink.fund.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Connects internal fund identity (FUND_SHARE_CLASS) to a provider's own
 * external identifier for that instrument. Business key per the
 * architecture doc: UNIQUE(provider_id, external_fund_id).
 */
@Entity
@Table(name = "fund_provider_mapping", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fund_provider_mapping_provider_external_id", columnNames = {"provider_id", "external_fund_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class FundProviderMapping {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_share_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_provider_mapping_share_class"))
    private FundShareClass fundShareClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_provider_mapping_provider"))
    private Provider provider;

    @Column(name = "external_fund_id", nullable = false)
    private String externalFundId;
}
