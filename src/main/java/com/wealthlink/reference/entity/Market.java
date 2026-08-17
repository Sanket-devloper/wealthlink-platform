package com.wealthlink.reference.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents an exchange/trading venue inside a country. Kept separate from
 * Country because a country can have multiple markets and settlement rules
 * can attach here later.
 */
@Entity
@Table(name = "market")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Market {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_market_country"))
    private Country country;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "mic_code", nullable = false)
    private String micCode;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MarketStatus status;
}
