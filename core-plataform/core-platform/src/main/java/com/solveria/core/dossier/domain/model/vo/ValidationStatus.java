package com.solveria.core.dossier.domain.model.vo;

import java.time.LocalDateTime;
import java.util.UUID;

public record ValidationStatus(
    ValidationState currentState, UUID reviewerId, LocalDateTime reviewDate, String rejectReason) {

  public ValidationStatus {
    if (currentState == null) {
      throw new IllegalArgumentException("currentState es requerido");
    }
    if (currentState == ValidationState.REJECTED
        && (rejectReason == null || rejectReason.isBlank())) {
      throw new IllegalArgumentException("rejectReason es requerido para estado REJECTED");
    }
  }

  public static ValidationStatus pending() {
    return new ValidationStatus(ValidationState.PENDING, null, null, null);
  }

  public ValidationStatus withState(
      ValidationState newState,
      UUID newReviewerId,
      LocalDateTime newReviewDate,
      String newRejectReason) {
    return new ValidationStatus(newState, newReviewerId, newReviewDate, newRejectReason);
  }
}
