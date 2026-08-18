package com.wealthlink.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * RBAC role (e.g. TRADER, RECONCILER, COMPLIANCE_OFFICER, ADMIN).
 * Exists so permissions aren't hardcoded per user.
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
