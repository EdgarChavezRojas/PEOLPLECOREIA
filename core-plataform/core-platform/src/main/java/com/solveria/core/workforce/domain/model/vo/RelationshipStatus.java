package com.solveria.core.workforce.domain.model.vo;

import lombok.Getter;

@Getter
public enum RelationshipStatus {
  DRAFT("DRAFT"),
  ACTIVE("ACTIVE"),
  SUSPENDED("SUSPENDED"),
  TERMINATED("FINISHED");

  private final String label;

  RelationshipStatus(String label) {
    this.label = label;
  }
}
