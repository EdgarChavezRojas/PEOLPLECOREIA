package com.solveria.core.workforce.domain.model.vo;

import lombok.Getter;

@Getter
public enum Gender {
  MALE("MALE"),
  FEMALE("FEMALE"),
  OTHER("OTHER");

  private final String label;

  Gender(String label) {
    this.label = label;
  }
}
