package com.solveria.core.workforce.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity: OrgHierarchy
 *
 * <p>Define relaciones padre-hijo en la jerarquía. Tiene identidad porque las jerarquías se
 * versionan en el tiempo (Effective Dating).
 */
public class OrgHierarchy {

  private UUID hierarchyId;
  private UUID parentUnitId;
  private UUID childUnitId;
  private String hierarchyType;
  private LocalDate effectiveDate;
  private LocalDate endDate;

  public OrgHierarchy() {}

  public OrgHierarchy(UUID hierarchyId, UUID parentUnitId, UUID childUnitId, String hierarchyType, LocalDate effectiveDate, LocalDate endDate) {
    this.hierarchyId = hierarchyId;
    this.parentUnitId = parentUnitId;
    this.childUnitId = childUnitId;
    this.hierarchyType = hierarchyType;
    this.effectiveDate = effectiveDate;
    this.endDate = endDate;
  }

  public UUID getHierarchyId() { return hierarchyId; }
  public void setHierarchyId(UUID hierarchyId) { this.hierarchyId = hierarchyId; }

  public UUID getParentUnitId() { return parentUnitId; }
  public void setParentUnitId(UUID parentUnitId) { this.parentUnitId = parentUnitId; }

  public UUID getChildUnitId() { return childUnitId; }
  public void setChildUnitId(UUID childUnitId) { this.childUnitId = childUnitId; }

  public String getHierarchyType() { return hierarchyType; }
  public void setHierarchyType(String hierarchyType) { this.hierarchyType = hierarchyType; }

  public LocalDate getEffectiveDate() { return effectiveDate; }
  public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

  public static OrgHierarchy create(UUID parentUnitId, UUID childUnitId, String hierarchyType, LocalDate effectiveDate) {
    if (parentUnitId == null || childUnitId == null) {
      throw new IllegalArgumentException("parentUnitId y childUnitId son requeridos");
    }
    if (parentUnitId.equals(childUnitId)) {
      throw new IllegalArgumentException("Una unidad no puede ser su propio padre");
    }
    return new OrgHierarchy(
            UUID.randomUUID(),
            parentUnitId,
            childUnitId,
            hierarchyType,
            effectiveDate != null ? effectiveDate : LocalDate.now(),
            null
    );
  }
}