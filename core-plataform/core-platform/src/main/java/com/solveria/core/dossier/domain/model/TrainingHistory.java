package com.solveria.core.dossier.domain.model;

import java.util.UUID;

public record TrainingHistory(UUID trainingId, String courseName, UUID docId) {

  public TrainingHistory {
    if (trainingId == null) {
      throw new IllegalArgumentException("trainingId es requerido");
    }
    if (courseName == null || courseName.isBlank()) {
      throw new IllegalArgumentException("courseName es requerido");
    }
  }
}
