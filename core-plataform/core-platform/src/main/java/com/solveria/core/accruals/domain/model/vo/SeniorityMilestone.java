package com.solveria.core.accruals.domain.model.vo;

import java.util.UUID;

public record SeniorityMilestone(
    UUID milestoneId, int monthsCompleted, SeniorityBaseType baseSmnType) {

  public SeniorityMilestone {
    if (milestoneId == null) {
      throw new IllegalArgumentException("milestoneId is required");
    }
    if (monthsCompleted <= 0) {
      throw new IllegalArgumentException("monthsCompleted must be positive");
    }
    if (baseSmnType == null) {
      throw new IllegalArgumentException("baseSmnType is required");
    }
  }
}
