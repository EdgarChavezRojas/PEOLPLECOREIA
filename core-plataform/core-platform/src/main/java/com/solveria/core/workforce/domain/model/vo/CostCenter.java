package com.solveria.core.workforce.domain.model.vo;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Value Object: CostCenter
 *
 * <p>Etiqueta contable inmutable. Si el código cambia, se asigna uno nuevo. No tiene ciclo de vida
 * propio.
 */
import java.util.Objects;

@Embeddable
public class CostCenter implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String costCode;
  private String description;

  public CostCenter() {}

  public CostCenter(String costCode, String description) {
    this.costCode = costCode;
    this.description = description;
  }

  public String getCostCode() { return costCode; }
  public void setCostCode(String costCode) { this.costCode = costCode; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public static CostCenter create(String costCode, String description) {
    if (costCode == null || costCode.isBlank()) {
      throw new IllegalArgumentException("costCode no puede estar vacío");
    }
    return new CostCenter(costCode, description);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CostCenter that = (CostCenter) o;
    return Objects.equals(costCode, that.costCode) &&
            Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(costCode, description);
  }
}
