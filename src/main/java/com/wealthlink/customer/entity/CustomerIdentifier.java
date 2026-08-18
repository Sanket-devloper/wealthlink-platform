package com.wealthlink.customer.entity;

import com.wealthlink.reference.entity.Country;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Separated from Customer because identifiers are sensitive, multi-valued,
 * and country-specific formats vary (Norwegian fodselsnummer vs Swedish
 * personnummer vs Danish CPR).
 */
@Entity
@Table(name = "customer_identifier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class CustomerIdentifier {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_identifier_customer"))
    private Customer customer;

    @Column(name = "id_type", nullable = false)
    private String idType;

    @Column(name = "id_value", nullable = false)
    private String idValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issuing_country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_identifier_country"))
    private Country issuingCountry;
}
