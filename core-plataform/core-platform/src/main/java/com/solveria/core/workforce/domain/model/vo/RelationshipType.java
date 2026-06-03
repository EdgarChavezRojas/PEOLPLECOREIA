package com.solveria.core.workforce.domain.model.vo;

public enum RelationshipType {
  LABOR("LABOR"),
  ACADEMIC("ACADEMIC"),
  INTERNSHIP("INTERSHIP"),
  EMPLOYEE("EMPLOYEE");

  private final String label;

  RelationshipType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
