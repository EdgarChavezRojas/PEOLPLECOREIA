package com.solveria.core.dossier.infrastructure.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "talent_inventory",
    indexes = {
      @Index(name = "idx_talent_inventory_relationship", columnList = "relationship_id"),
      @Index(name = "idx_talent_inventory_tenant", columnList = "tenant_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentInventoryJpa {

  @Id
  @Column(name = "inventory_id")
  private UUID inventoryId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @OneToMany(
      mappedBy = "inventory",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<PerformanceSnapshotJpa> performanceSnapshots = new ArrayList<>();

  @OneToMany(
      mappedBy = "inventory",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<SkillSetJpa> skillSets = new ArrayList<>();

  @OneToMany(
      mappedBy = "inventory",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<TrainingHistoryJpa> trainingHistory = new ArrayList<>();
}
