package com.wealthlink.reference.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Country master data. Adding Finland later is a row insert, not a schema change.
 */
@Entity
@Table(name = "country")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Country {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "iso_code", length = 2, nullable = false, unique = true)
    private String isoCode;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "default_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_country_default_currency"))
    private Currency defaultCurrency;

    @Column(name = "timezone", nullable = false)
    private String timezone;
}
