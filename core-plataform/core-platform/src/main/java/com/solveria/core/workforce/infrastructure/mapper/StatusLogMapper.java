package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.StatusLog;
import com.solveria.core.workforce.infrastructure.jpa.StatusLogJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatusLogMapper {

  @Mapping(target = "relationship", ignore = true)
  StatusLogJpa toJpa(StatusLog statusLog);

  @Mapping(target = "relationshipId", source = "relationship.relationshipId")
  StatusLog toDomain(StatusLogJpa jpa);
}
