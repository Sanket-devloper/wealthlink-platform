package com.wealthlink.account.entity;

import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A single ACCOUNT entity with a type discriminator, not two separate tables.
 * Cash and investment accounts share every structural attribute (owner,
 * currency, country, status, ledger backing) and only differ in behavior
 * (what can post to them), which is enforced at the application/ledger
 * level, not the schema level.
 */
@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Account {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_currency"))
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_country"))
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountOwner> owners = new HashSet<>();

    @PrePersist
    void onCreate() {
        if (this.openedAt == null) {
            this.openedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }
}
