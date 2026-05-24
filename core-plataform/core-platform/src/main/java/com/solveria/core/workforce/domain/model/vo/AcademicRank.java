package com.solveria.core.workforce.domain.model.vo;

public enum AcademicRank {
  ASSITANT("Auxiliar"),
  DEPUTY("Adjunto"),
  HOLDER("Titular"),
  RESEARCHER("Investigador");

  private final String label;

  AcademicRank(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
