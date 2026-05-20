package com.solveria.core.dossier.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.model.PerformanceSnapshot;
import com.solveria.core.dossier.domain.model.SkillSet;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.domain.model.TrainingHistory;
import com.solveria.core.dossier.infrastructure.jpa.PerformanceSnapshotJpa;
import com.solveria.core.dossier.infrastructure.jpa.SkillSetJpa;
import com.solveria.core.dossier.infrastructure.jpa.TalentInventoryJpa;
import com.solveria.core.dossier.infrastructure.jpa.TrainingHistoryJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TalentInventoryMapper {

  TalentInventoryJpa toJpa(TalentInventory inventory);

  PerformanceSnapshotJpa toJpa(PerformanceSnapshot snapshot);

  SkillSetJpa toJpa(SkillSet skillSet);

  TrainingHistoryJpa toJpa(TrainingHistory trainingHistory);

  default TalentInventory toDomain(TalentInventoryJpa jpa) {
    if (jpa == null) {
      return null;
    }
    List<PerformanceSnapshot> snapshots =
        jpa.getPerformanceSnapshots() == null
            ? List.of()
            : jpa.getPerformanceSnapshots().stream().map(this::toDomain).toList();
    List<SkillSet> skills =
        jpa.getSkillSets() == null
            ? List.of()
            : jpa.getSkillSets().stream().map(this::toDomain).toList();
    List<TrainingHistory> trainings =
        jpa.getTrainingHistory() == null
            ? List.of()
            : jpa.getTrainingHistory().stream().map(this::toDomain).toList();

    return new TalentInventory(
        jpa.getInventoryId(),
        jpa.getRelationshipId(),
        jpa.getTenantId(),
        snapshots,
        skills,
        trainings);
  }

  default PerformanceSnapshot toDomain(PerformanceSnapshotJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new PerformanceSnapshot(jpa.getSnapshotId(), jpa.getEvalPeriod(), jpa.getScore());
  }

  default SkillSet toDomain(SkillSetJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new SkillSet(jpa.getSkillId(), jpa.getSkillName(), jpa.getProficiency());
  }

  default TrainingHistory toDomain(TrainingHistoryJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new TrainingHistory(jpa.getTrainingId(), jpa.getCourseName(), jpa.getDocId());
  }

  @AfterMapping
  default void setBackReferences(@MappingTarget TalentInventoryJpa jpa, TalentInventory inventory) {
    if (jpa == null) {
      return;
    }
    if (jpa.getPerformanceSnapshots() != null) {
      for (PerformanceSnapshotJpa snapshot : jpa.getPerformanceSnapshots()) {
        snapshot.setInventory(jpa);
      }
    }
    if (jpa.getSkillSets() != null) {
      for (SkillSetJpa skill : jpa.getSkillSets()) {
        skill.setInventory(jpa);
      }
    }
    if (jpa.getTrainingHistory() != null) {
      for (TrainingHistoryJpa training : jpa.getTrainingHistory()) {
        training.setInventory(jpa);
      }
    }
  }

  default String toEventPayload(TalentInventory inventory, DomainEvent event) {
    if (inventory == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "inventoryId", inventory.getInventoryId(),
            "relationshipId", inventory.getRelationshipId(),
            "tenantId", inventory.getTenantId(),
            "eventType", event.getClass().getSimpleName());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando TalentInventory a JSON", e);
    }
  }
}
