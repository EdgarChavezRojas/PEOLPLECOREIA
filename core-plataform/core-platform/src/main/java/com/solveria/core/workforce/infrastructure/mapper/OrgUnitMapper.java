package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.application.dto.OrgUnitResponse;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.infrastructure.jpa.OrgHierarchyJpa;
import com.solveria.core.workforce.infrastructure.jpa.OrgUnitJpa;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true),
    uses = {OrgHierarchyMapper.class})
public interface OrgUnitMapper {

  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "orgHierarchies", source = "hierarchies")
  OrgUnitJpa toJpa(OrgUnit orgUnit);

  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "hierarchies", source = "orgHierarchies")
  OrgUnit toDomain(OrgUnitJpa jpa);

  @Mapping(
      target = "unitType",
      expression = "java(orgUnit.getUnitType() != null ? orgUnit.getUnitType().name() : null)")
  @Mapping(target = "costCode", source = "costCenter.costCode")
  @Mapping(target = "costDescription", source = "costCenter.description")
  OrgUnitResponse toResponse(OrgUnit orgUnit);

  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "orgHierarchies", ignore = true)
  void updateJpa(OrgUnit orgUnit, @MappingTarget OrgUnitJpa orgUnitJpa);

  @AfterMapping
  default void setBackReference(@MappingTarget OrgUnitJpa orgUnitJpa) {
    if (orgUnitJpa.getOrgHierarchies() == null) {
      return;
    }
    for (OrgHierarchyJpa hierarchy : orgUnitJpa.getOrgHierarchies()) {
      hierarchy.setChildUnit(orgUnitJpa);
      hierarchy.setTenantId(orgUnitJpa.getTenantId());
    }
  }

  default String toEventPayload(OrgUnit orgUnit) {
    if (orgUnit == null) return "{}";

    try {
      return new com.fasterxml.jackson.databind.ObjectMapper()
          .writeValueAsString(
              java.util.Map.ofEntries(
                  java.util.Map.entry("unitId", orgUnit.getUnitId()),
                  java.util.Map.entry("tenantId", orgUnit.getTenantId()),
                  java.util.Map.entry("parentId", orgUnit.getParentId()),
                  java.util.Map.entry("name", orgUnit.getName()),
                  java.util.Map.entry("unitType", orgUnit.getUnitType().name()),
                  java.util.Map.entry("isRoot", orgUnit.getIsRoot())));
    } catch (Exception e) {
      throw new RuntimeException("Error serializando OrgUnit a JSON", e);
    }
  }
}
