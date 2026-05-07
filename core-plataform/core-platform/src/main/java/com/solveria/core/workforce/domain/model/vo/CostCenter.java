package com.solveria.core.workforce.domain.model.vo;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object: CostCenter
 *
 * <p>Etiqueta contable inmutable. Si el código cambia, se asigna uno nuevo. No tiene ciclo de vida
 * propio.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CostCenter implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private String costCode;
  private String description;

  public static CostCenter create(String costCode, String description) {
    if (costCode == null || costCode.isBlank()) {
      throw new IllegalArgumentException("costCode no puede estar vacío");
    }
    return new CostCenter(costCode, description);
  }
}
