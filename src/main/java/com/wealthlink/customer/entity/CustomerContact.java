package com.wealthlink.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * One customer, many contact points, one primary per type
 * (enforced with a partial unique index in the DB migration).
 */
@Entity
@Table(name = "customer_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class CustomerContact {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_contact_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private ContactType contactType;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;
}
