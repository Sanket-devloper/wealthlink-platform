package com.wealthlink.reference.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Master currency list (NOK, SEK, DKK, EUR, ...).
 * Nothing else in the schema references currency by string code - always by FK -
 * so adding a new currency later is a data insert, not a migration.
 */
@Entity
@Table(name = "currency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Currency {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "iso_code", length = 3, nullable = false, unique = true)
    private String isoCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "minor_unit_digits", nullable = false)
    private Short minorUnitDigits;
}
