package com.wealthlink.reconciliation.entity;

import com.wealthlink.identity.entity.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_resolution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ReconciliationResolution {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reconciliation_item_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_recon_res_item"))
    private ReconciliationItem reconciliationItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resolved_by", nullable = false, foreignKey = @ForeignKey(name = "fk_recon_res_user"))
    private AppUser resolvedBy;

    @Column(name = "resolution_type", nullable = false, length = 100)
    private String resolutionType;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at", nullable = false, updatable = false)
    private Instant resolvedAt;

    @PrePersist
    void onCreate() {
        if (this.resolvedAt == null) {
            this.resolvedAt = Instant.now();
        }
    }
}