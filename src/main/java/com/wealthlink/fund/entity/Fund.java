package com.wealthlink.fund.entity;

import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fund master record.
 * <p>
 * NOTE on FKs: Dev 1's Foundation module (Currency / Country) is already
 * merged into this codebase, so this entity wires real {@code @ManyToOne}
 * relationships directly instead of the temporary UUID-stub pattern the
 * architecture doc describes for when Dev 1 hasn't landed yet (Section 9 /
 * DEV2-D1). Day-1/Day-2 of the Dev 2 plan are effectively collapsed here.
 */
@Entity
@Table(name = "fund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Fund {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "isin", nullable = false, unique = true, length = 12)
    private String isin;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_base_currency"))
    private Currency baseCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domicile_country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fund_domicile_country"))
    private Country domicileCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FundStatus status;

    @Column(name = "inception_date")
    private LocalDate inceptionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FundShareClass> shareClasses = new HashSet<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = FundStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
