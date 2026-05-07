package com.solveria.core.financial.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Command: Ajustar la distribución del costo laboral. W2: Transfer Split Proration. */
public record AdjustCostSplitCommand(
    UUID sourceId, List<SplitEntry> splits, String approverUserId) {
  public AdjustCostSplitCommand {
    if (sourceId == null) {
      throw new IllegalArgumentException("sourceId es obligatorio");
    }
    if (splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("splits no puede estar vacío");
    }
    if (approverUserId == null || approverUserId.isBlank()) {
      throw new IllegalArgumentException("approverUserId es obligatorio para SoD");
    }
  }

  /** Entrada individual de split. */
  public record SplitEntry(UUID unitId, BigDecimal percentage, LocalDate effectiveDate) {
    public SplitEntry {
      if (unitId == null) {
        throw new IllegalArgumentException("unitId es obligatorio");
      }
      if (percentage == null) {
        throw new IllegalArgumentException("percentage es obligatorio");
      }
      if (effectiveDate == null) {
        throw new IllegalArgumentException("effectiveDate es obligatorio");
      }
    }
  }
}
