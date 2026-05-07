package com.solveria.core.dossier.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PerformanceSnapshot(UUID snapshotId, String evalPeriod, BigDecimal score) {

  public PerformanceSnapshot {
    if (snapshotId == null) {
      throw new IllegalArgumentException("snapshotId es requerido");
    }
    if (evalPeriod == null || evalPeriod.isBlank()) {
      throw new IllegalArgumentException("evalPeriod es requerido");
    }
    if (score == null) {
      throw new IllegalArgumentException("score es requerido");
    }
  }
}
