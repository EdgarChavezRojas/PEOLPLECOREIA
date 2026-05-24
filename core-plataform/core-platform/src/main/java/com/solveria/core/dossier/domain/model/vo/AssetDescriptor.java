package com.solveria.core.dossier.domain.model.vo;

public record AssetDescriptor(AssetCategory category, String techSpecsJson, String initialState) {

  public AssetDescriptor {
    if (category == null) {
      throw new IllegalArgumentException("category es requerido");
    }
    if (initialState == null || initialState.isBlank()) {
      throw new IllegalArgumentException("initialState es requerido");
    }
  }
}
