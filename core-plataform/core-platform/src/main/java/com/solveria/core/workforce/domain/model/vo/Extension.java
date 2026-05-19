package com.solveria.core.workforce.domain.model.vo;

public enum Extension {
  SCZ("Santa Cruz"),
  LP("La Paz"),
  CB("Cochabamba"),
  OR("Oruro"),
  PT("Potosí"),
  TJ("Tarija"),
  BE("Beni"),
  PA("Pando");

  private final String name;

  Extension(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}