package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.application.dto.PositionResponse;
import com.solveria.core.workforce.domain.model.Position;
import com.solveria.core.workforce.infrastructure.jpa.PositionJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {JobMapper.class})
public interface PositionMapper {

  PositionJpa toJpa(Position position);

  Position toDomain(PositionJpa jpa);

  @Mapping(target = "job", ignore = true)
  void updateJpa(Position position, @MappingTarget PositionJpa positionJpa);

  @Mapping(target = "jobId", source = "job.jobId")
  @Mapping(
      target = "status",
      expression = "java(position.getStatus() != null ? position.getStatus().name() : null)")
  @Mapping(target = "maxSlots", source = "headcountPlan.maxSlots")
  @Mapping(target = "currentSlots", source = "headcountPlan.currentSlots")
  @Mapping(
      target = "availableSlots",
      expression =
          "java(position.getHeadcountPlan() != null ? position.getHeadcountPlan().getAvailableSlots() : null)")
  PositionResponse toResponse(Position position);

  default String toEventPayload(Position position) {
    if (position == null) return "{}";

    try {
      return new com.fasterxml.jackson.databind.ObjectMapper()
          .writeValueAsString(
              java.util.Map.ofEntries(
                  java.util.Map.entry("positionId", position.getPositionId()),
                  java.util.Map.entry("unitId", position.getUnitId()),
                  java.util.Map.entry("jobId", position.getJobId()),
                  java.util.Map.entry("isBudgeted", position.getIsBudgeted()),
                  java.util.Map.entry("maxSlots", position.getHeadcountPlan().getMaxSlots()),
                  java.util.Map.entry(
                      "currentSlots", position.getHeadcountPlan().getCurrentSlots())));
    } catch (Exception e) {
      throw new RuntimeException("Error serializando Position a JSON", e);
    }
  }
}
