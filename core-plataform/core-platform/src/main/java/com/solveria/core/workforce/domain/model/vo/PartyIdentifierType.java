package com.solveria.core.workforce.domain.model.vo;

public enum PartyIdentifierType {
  CI("Carnet de Identidad"),
  PASSPORT("Pasaporte");

  private final String description;

  PartyIdentifierType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
