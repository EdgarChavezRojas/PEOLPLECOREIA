package com.solveria.core.workforce.domain.model.vo;

public enum Gender {
  MALE("MALE"),
  FEMALE("FEMALE"),
  OTHER("OTHER");

  private final String label;

  Gender(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
