package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.OrgUnitAssignedChangedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitExtensionUpdatedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitGeographicMovedEvent;
import com.solveria.core.workforce.domain.model.vo.CostCenter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrgUnit extends DomainRoot {

  private UUID unitId;
  private UUID tenantId;
  private UUID parentId;
  private String name;
  private OrgUnitType unitType;
  private CostCenter costCenter;
  private String geoCoords;
  private Boolean isRoot;
  private List<OrgHierarchy> hierarchies;

  public OrgUnit() {
    this.hierarchies = new ArrayList<>();
  }

  public OrgUnit(UUID unitId, UUID tenantId, UUID parentId, String name, OrgUnitType unitType, CostCenter costCenter, String geoCoords, Boolean isRoot, List<OrgHierarchy> hierarchies) {
    this.unitId = unitId;
    this.tenantId = tenantId;
    this.parentId = parentId;
    this.name = name;
    this.unitType = unitType;
    this.costCenter = costCenter;
    this.geoCoords = geoCoords;
    this.isRoot = isRoot;
    this.hierarchies = hierarchies != null ? hierarchies : new ArrayList<>();
  }

  public enum OrgUnitType {
    ADMINISTRATIVE("Administrativa"),
    ACADEMIC("Academica"),
    COMMERCIAL("Comercial");

    private final String label;

    OrgUnitType(String label) { this.label = label; }
    public String getLabel() { return label; }
  }

  // Getters y Setters
  public UUID getUnitId() { return unitId; }
  public void setUnitId(UUID unitId) { this.unitId = unitId; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getParentId() { return parentId; }
  public void setParentId(UUID parentId) { this.parentId = parentId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public OrgUnitType getUnitType() { return unitType; }
  public void setUnitType(OrgUnitType unitType) { this.unitType = unitType; }

  public CostCenter getCostCenter() { return costCenter; }
  public void setCostCenter(CostCenter costCenter) { this.costCenter = costCenter; }

  public String getGeoCoords() { return geoCoords; }
  public void setGeoCoords(String geoCoords) { this.geoCoords = geoCoords; }

  public Boolean getIsRoot() { return isRoot; }
  public void setIsRoot(Boolean isRoot) { this.isRoot = isRoot; }

  public List<OrgHierarchy> getHierarchies() { return hierarchies; }
  public void setHierarchies(List<OrgHierarchy> hierarchies) { this.hierarchies = hierarchies != null ? hierarchies : new ArrayList<>(); }

  public static OrgUnit createRoot(UUID tenantId, String name, OrgUnitType unitType, CostCenter costCenter) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name es requerido");
    }
    return new OrgUnit(UUID.randomUUID(), tenantId, null, name, unitType, costCenter, null, true, new ArrayList<>());
  }

  public static OrgUnit createChild(UUID tenantId, UUID parentId, String name, OrgUnitType unitType, CostCenter costCenter) {
    if (parentId == null) {
      throw new IllegalArgumentException("parentId no puede ser nulo para unidades no-raiz");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name es requerido");
    }
    return new OrgUnit(UUID.randomUUID(), tenantId, parentId, name, unitType, costCenter, null, false, new ArrayList<>());
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
    registerEvent(new OrgUnitGeographicMovedEvent(unitId, newGeoCoords, tenantId));
  }

  public void addHierarchy(OrgHierarchy hierarchy) {
    if (hierarchy == null) {
      throw new IllegalArgumentException("hierarchy no puede ser nulo");
    }
    this.hierarchies.add(hierarchy);
  }
}