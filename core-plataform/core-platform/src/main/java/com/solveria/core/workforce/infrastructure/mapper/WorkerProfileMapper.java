package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.WorkerProfile;
import com.solveria.core.workforce.infrastructure.jpa.WorkerProfileJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkerProfileMapper {

  @Mapping(target = "relationship", ignore = true)
  WorkerProfileJpa toJpa(WorkerProfile workerProfile);

  @Mapping(target = "relationshipId", source = "relationship.relationshipId")
  WorkerProfile toDomain(WorkerProfileJpa jpa);
}
