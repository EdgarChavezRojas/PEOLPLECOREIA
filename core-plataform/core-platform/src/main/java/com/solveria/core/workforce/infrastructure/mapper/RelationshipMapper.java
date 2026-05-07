package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import com.solveria.core.workforce.infrastructure.jpa.StatusLogJpa;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {WorkerProfileMapper.class, AcademicProfileMapper.class, StatusLogMapper.class})
public interface RelationshipMapper {

  RelationshipJpa toJpa(Relationship relationship);

  Relationship toDomain(RelationshipJpa jpa);

  @Mapping(
      target = "relationType",
      expression =
          "java(relationship.getRelationType() != null ? relationship.getRelationType().name() : null)")
  @Mapping(
      target = "currentStatus",
      expression =
          "java(relationship.getCurrentStatus() != null ? relationship.getCurrentStatus().name() : null)")
  RelationshipResponse toResponse(Relationship relationship);

  @AfterMapping
  default void setBackReference(@MappingTarget RelationshipJpa relationshipJpa) {
    if (relationshipJpa.getWorkerProfile() != null) {
      relationshipJpa.getWorkerProfile().setRelationship(relationshipJpa);
    }
    if (relationshipJpa.getAcademicProfile() != null) {
      relationshipJpa.getAcademicProfile().setRelationship(relationshipJpa);
    }
    if (relationshipJpa.getStatusLogs() != null) {
      for (StatusLogJpa statusLog : relationshipJpa.getStatusLogs()) {
        statusLog.setRelationship(relationshipJpa);
      }
    }
  }

  default String toEventPayload(Relationship relationship) {
    if (relationship == null) return "{}";

    try {
      return new com.fasterxml.jackson.databind.ObjectMapper()
          .writeValueAsString(
              java.util.Map.ofEntries(
                  java.util.Map.entry("relationshipId", relationship.getRelationshipId()),
                  java.util.Map.entry("personId", relationship.getPersonId()),
                  java.util.Map.entry("tenantId", relationship.getTenantId()),
                  java.util.Map.entry("relationType", relationship.getRelationType().name()),
                  java.util.Map.entry("currentStatus", relationship.getCurrentStatus().name()),
                  java.util.Map.entry(
                      "hireDate",
                      relationship.getHireDate() != null
                          ? relationship.getHireDate().toString()
                          : null)));
    } catch (Exception e) {
      throw new RuntimeException("Error serializando Relationship a JSON", e);
    }
  }
}
