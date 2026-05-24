package com.solveria.core.workforce.domain.model.vo;

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object: HeadcountPlan
 *
 * <p>Plan de ocupación de plazas.
 *
 * <p>Invariante: No se puede exceder el límite de slots.
 */
@Embeddable
public class HeadcountPlan implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Integer maxSlots;
  private Integer currentSlots;

  public HeadcountPlan() {}

  public HeadcountPlan(Integer maxSlots, Integer currentSlots) {
    this.maxSlots = maxSlots;
    this.currentSlots = currentSlots;
  }

  public Integer getMaxSlots() {
    return maxSlots;
  }

  public void setMaxSlots(Integer maxSlots) {
    this.maxSlots = maxSlots;
  }

  public Integer getCurrentSlots() {
    return currentSlots;
  }

  public void setCurrentSlots(Integer currentSlots) {
    this.currentSlots = currentSlots;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HeadcountPlan that = (HeadcountPlan) o;
    return Objects.equals(maxSlots, that.maxSlots)
        && Objects.equals(currentSlots, that.currentSlots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxSlots, currentSlots);
  }
}
