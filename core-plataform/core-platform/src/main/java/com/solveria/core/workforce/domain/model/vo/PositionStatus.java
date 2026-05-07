package com.solveria.core.workforce.domain.model.vo;

import lombok.Getter;

@Getter
public enum PositionStatus {
  VACANT("VACANT"),
  OCCUPIED("OCCUPIED"),
  RESERVED("RESERVED");

  private final String label;

  PositionStatus(String label) {
    this.label = label;
  }
}
