package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.OrgUnitAssignedChangedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitExtensionUpdatedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitGeographicMovedEvent;
import com.solveria.core.workforce.domain.model.vo.CostCenter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgUnit extends DomainRoot {

  private UUID unitId;
  private UUID tenantId;
  private UUID parentId;
  private String name;
  private OrgUnitType unitType;
  private CostCenter costCenter;
  private String geoCoords;
  private Boolean isRoot;

  @Builder.Default private List<OrgHierarchy> hierarchies = new ArrayList<>();



  public enum OrgUnitType {
    ADMINISTRATIVE("Administrativa"),
    ACADEMIC("Academica"),
    COMMERCIAL("Comercial");

    private final String label;

    OrgUnitType(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  public static OrgUnit createRoot(
      UUID tenantId, String name, OrgUnitType unitType, CostCenter costCenter) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name es requerido");
    }

    return OrgUnit.builder()
        .unitId(UUID.randomUUID())
        .tenantId(tenantId)
        .parentId(null)
        .name(name)
        .unitType(unitType)
        .costCenter(costCenter)
        .isRoot(true)
        .hierarchies(new ArrayList<>())
        .build();
  }

  public static OrgUnit createChild(
      UUID tenantId, UUID parentId, String name, OrgUnitType unitType, CostCenter costCenter) {
    if (parentId == null) {
      throw new IllegalArgumentException("parentId no puede ser nulo para unidades no-raiz");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name es requerido");
    }

    return OrgUnit.builder()
        .unitId(UUID.randomUUID())
        .tenantId(tenantId)
        .parentId(parentId)
        .name(name)
        .unitType(unitType)
        .costCenter(costCenter)
        .isRoot(false)
        .hierarchies(new ArrayList<>())
        .build();
  }

  public void changeAssignment(UUID newParentId) {
    this.parentId = newParentId;
    registerEvent(new OrgUnitAssignedChangedEvent(unitId, newParentId, Instant.now()));
  }

  public void updateCostCenter(CostCenter newCostCenter) {
    this.costCenter = newCostCenter;
    registerEvent(new OrgUnitExtensionUpdatedEvent(unitId, newCostCenter, Instant.now()));
  }

  public void updateGeoCoords(String newGeoCoords) {
    this.geoCoords = newGeoCoords;
    registerEvent(new OrgUnitGeographicMovedEvent(unitId, newGeoCoords, Instant.now()));
  }

  public void addHierarchy(OrgHierarchy hierarchy) {
    if (hierarchy == null) {
      throw new IllegalArgumentException("hierarchy no puede ser nulo");
    }
    hierarchies.add(hierarchy);
  }


}
