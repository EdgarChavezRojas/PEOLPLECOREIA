package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.OrgHierarchy;
import com.solveria.core.workforce.infrastructure.jpa.OrgHierarchyJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrgHierarchyMapper {

  @Mapping(target = "childUnit", ignore = true)
  OrgHierarchyJpa toJpa(OrgHierarchy orgHierarchy);

  @Mapping(target = "childUnitId", source = "childUnit.unitId")
  OrgHierarchy toDomain(OrgHierarchyJpa jpa);
}
