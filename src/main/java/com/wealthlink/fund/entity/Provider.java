package com.wealthlink.fund.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Generic external data provider (pricing vendor, custodian feed, etc).
 * Kept generic per the architecture doc - both FUND_PROVIDER_MAPPING and
 * FUND_PRICE reference this same table rather than provider-specific ones.
 */
@Entity
@Table(name = "provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Provider {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProviderStatus status;

    @PrePersist
    void onCreate() {
        if (this.status == null) {
            this.status = ProviderStatus.ACTIVE;
        }
    }
}
