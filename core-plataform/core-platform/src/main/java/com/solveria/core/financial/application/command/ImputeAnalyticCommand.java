package com.solveria.core.financial.application.command;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command: Imputación Analítica Territorial con prorrateo mid-month. W4: Territorial Analytic
 * Imputation.
 */
public record ImputeAnalyticCommand(
    UUID sourceId,
    UUID personId,
    UUID oldUnitId,
    UUID newUnitId,
    LocalDate transferDate,
    LocalDate periodStart,
    LocalDate periodEnd) {
  public ImputeAnalyticCommand {
    if (sourceId == null) {
      throw new IllegalArgumentException("sourceId es obligatorio");
    }
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (oldUnitId == null) {
      throw new IllegalArgumentException("oldUnitId es obligatorio");
    }
    if (newUnitId == null) {
      throw new IllegalArgumentException("newUnitId es obligatorio");
    }
    if (transferDate == null) {
      throw new IllegalArgumentException("transferDate es obligatorio");
    }
    if (periodStart == null || periodEnd == null) {
      throw new IllegalArgumentException("Período de imputación es obligatorio");
    }
  }
}
