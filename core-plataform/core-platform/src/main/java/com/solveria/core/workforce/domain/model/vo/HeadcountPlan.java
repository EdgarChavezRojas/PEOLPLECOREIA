package com.solveria.core.workforce.domain.model.vo;

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object: HeadcountPlan
 *
 * <p>Plan de ocupación de plazas.
 *
 * <p>Invariante: No se puede exceder el límite de slots.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HeadcountPlan implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Integer maxSlots;
  private Integer currentSlots;

  public static HeadcountPlan create(Integer maxSlots) {
    if (maxSlots == null || maxSlots <= 0) {
      throw new IllegalArgumentException("maxSlots debe ser mayor a 0");
    }
    return new HeadcountPlan(maxSlots, 0);
  }

  public boolean hasVacancy() {
    return currentSlots < maxSlots;
  }

  public void occupy() {
    if (!hasVacancy()) {
      throw new IllegalStateException(
          "No hay plazas vacantes. Control de Plazas (Headcount): "
              + "Impide asignar personal a una posición sin plaza vacante autorizada presupuestariamente.");
    }
    currentSlots++;
  }

  public void vacate() {
    if (currentSlots > 0) {
      currentSlots--;
    }
  }

  public Integer getAvailableSlots() {
    return maxSlots - currentSlots;
  }
}
