package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.DocentAcademicTitleVerifiedEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TalentInventory extends DomainRoot {

  private UUID inventoryId;
  private UUID relationshipId;
  private UUID tenantId;
  private List<PerformanceSnapshot> performanceSnapshots;
  private List<SkillSet> skillSets;
  private List<TrainingHistory> trainingHistory;

  public TalentInventory() {
  }

  public TalentInventory(UUID inventoryId, UUID relationshipId, UUID tenantId,
                         List<PerformanceSnapshot> performanceSnapshots,
                         List<SkillSet> skillSets,
                         List<TrainingHistory> trainingHistory) {
    this.inventoryId = inventoryId;
    this.relationshipId = relationshipId;
    this.tenantId = tenantId;
    this.performanceSnapshots = performanceSnapshots;
    this.skillSets = skillSets;
    this.trainingHistory = trainingHistory;
  }

  public static TalentInventory create(UUID relationshipId, UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId es requerido");
    }
    return new TalentInventory(
            UUID.randomUUID(),
            relationshipId,
            tenantId,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
    );
  }

  public void addPerformanceSnapshot(PerformanceSnapshot snapshot) {
    if (snapshot == null) {
      throw new IllegalArgumentException("snapshot es requerido");
    }
    performanceSnapshots.add(snapshot);
  }

  public void addSkillSet(SkillSet skillSet) {
    if (skillSet == null) {
      throw new IllegalArgumentException("skillSet es requerido");
    }
    skillSets.add(skillSet);
  }

  public void addTrainingHistory(TrainingHistory training, boolean verifiedTitle) {
    if (training == null) {
      throw new IllegalArgumentException("training es requerido");
    }
    trainingHistory.add(training);
    if (verifiedTitle) {
      String titleLevel = resolveTitleLevel(training);
      registerEvent(DocentAcademicTitleVerifiedEvent.now(this.relationshipId, titleLevel, true));
    }
  }

  private static String resolveTitleLevel(TrainingHistory training) {
    String courseName = training.courseName();
    if (courseName == null || courseName.isBlank()) {
      throw new IllegalArgumentException();
    }
    return courseName.trim();
  }

  public UUID getInventoryId() {
    return inventoryId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public List<PerformanceSnapshot> getPerformanceSnapshots() {
    return performanceSnapshots;
  }

  public List<SkillSet> getSkillSets() {
    return skillSets;
  }

  public List<TrainingHistory> getTrainingHistory() {
    return trainingHistory;
  }
}