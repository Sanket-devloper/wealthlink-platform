package com.wealthlink.account.entity;

import com.wealthlink.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Junction table so joint accounts (two customers, one account) are
 * representable without denormalizing owner onto Account.
 */
@Entity
@Table(name = "account_owner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AccountOwner {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_owner_account"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_owner_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_role", nullable = false, length = 20)
    private OwnershipRole ownershipRole;
}
