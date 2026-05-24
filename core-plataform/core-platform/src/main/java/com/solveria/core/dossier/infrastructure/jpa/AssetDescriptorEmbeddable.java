package com.solveria.core.dossier.infrastructure.jpa;

import com.solveria.core.dossier.domain.model.vo.AssetCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AssetDescriptorEmbeddable {

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private AssetCategory category;

  @Column(name = "tech_specs", columnDefinition = "JSONB")
  private String techSpecsJson;

  @Column(name = "initial_state")
  private String initialState;
}
