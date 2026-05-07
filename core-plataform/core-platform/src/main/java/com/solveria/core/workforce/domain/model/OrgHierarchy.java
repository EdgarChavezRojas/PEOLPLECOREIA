package com.solveria.core.workforce.domain.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity: OrgHierarchy
 *
 * <p>Define relaciones padre-hijo en la jerarquía. Tiene identidad porque las jerarquías se
 * versionan en el tiempo (Effective Dating).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgHierarchy {

  private UUID hierarchyId;
  private UUID parentUnitId;
  private UUID childUnitId;
  private String hierarchyType; // Administrativa, Funcional, Académica
  private LocalDate effectiveDate;
  private LocalDate endDate;

  public static OrgHierarchy create(
      UUID parentUnitId, UUID childUnitId, String hierarchyType, LocalDate effectiveDate) {
    if (parentUnitId == null || childUnitId == null) {
      throw new IllegalArgumentException("parentUnitId y childUnitId son requeridos");
    }
    if (parentUnitId.equals(childUnitId)) {
      throw new IllegalArgumentException("Una unidad no puede ser su propio padre");
    }

    return OrgHierarchy.builder()
        .hierarchyId(UUID.randomUUID())
        .parentUnitId(parentUnitId)
        .childUnitId(childUnitId)
        .hierarchyType(hierarchyType)
        .effectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now())
        .build();
  }
}
