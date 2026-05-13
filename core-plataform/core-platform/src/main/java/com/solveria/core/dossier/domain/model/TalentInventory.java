package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.event.DossierEventType;
import com.solveria.core.shared.events.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentInventory extends DomainRoot {

  private UUID inventoryId;
  private UUID relationshipId;
  private UUID tenantId;
  private List<PerformanceSnapshot> performanceSnapshots;
  private List<SkillSet> skillSets;
  private List<TrainingHistory> trainingHistory;



  public static TalentInventory create(UUID relationshipId, UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId es requerido");
    }
    return TalentInventory.builder()
        .inventoryId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .tenantId(tenantId)
        .performanceSnapshots(new ArrayList<>())
        .skillSets(new ArrayList<>())
        .trainingHistory(new ArrayList<>())
        .build();
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
      registerEvent(DossierEvent.now(DossierEventType.DOCENT_ACADEMIC_TITLE_VERIFIED));
    }
  }


}
