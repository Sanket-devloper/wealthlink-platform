package com.wealthlink.portfolio.entity;

import com.wealthlink.account.entity.Account;
import com.wealthlink.reference.entity.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Portfolio {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_account"))
    private Account account;

    @Column(name = "portfolio_number", nullable = false, unique = true)
    private String portfolioNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_type", nullable = false, length = 20)
    private PortfolioType portfolioType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_currency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_base_currency"))
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PortfolioStatus status;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @PrePersist
    void onCreate() {
        if (this.openedAt == null) {
            this.openedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = PortfolioStatus.ACTIVE;
        }
    }
}
