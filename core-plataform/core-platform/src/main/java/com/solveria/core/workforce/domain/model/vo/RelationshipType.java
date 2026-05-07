package com.solveria.core.workforce.domain.model.vo;

import lombok.Getter;

@Getter
public enum RelationshipType {
  LABOR("LABOR"),
  ACADEMIC("ACADEMIC"),
  INTERNSHIP("INTERSHIP");

  private final String label;

  RelationshipType(String label) {
    this.label = label;
  }
}
