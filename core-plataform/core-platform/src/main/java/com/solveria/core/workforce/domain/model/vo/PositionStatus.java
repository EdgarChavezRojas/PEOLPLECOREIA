package com.solveria.core.workforce.domain.model.vo;

public enum PositionStatus {
  VACANT("VACANT"),
  OCCUPIED("OCCUPIED"),
  RESERVED("RESERVED");

  private final String label;

  PositionStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
