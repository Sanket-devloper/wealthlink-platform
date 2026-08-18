package com.wealthlink.marketdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Named source of FX rates (e.g. an ECB feed, a custodian feed). Kept
 * separate from PROVIDER because an FX rate source is not necessarily a
 * fund-pricing provider, even though both are "external data sources".
 */
@Entity
@Table(name = "fx_rate_source")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class FxRateSource {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
