package com.solveria.core.dossier.infrastructure.jpa;

import com.solveria.core.dossier.domain.model.vo.ProficiencyLevel;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_set")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillSetJpa extends BaseEntity {

  @Id
  @Column(name = "skill_id")
  private UUID skillId;

  @Column(name = "skill_name", nullable = false)
  private String skillName;

  @Enumerated(EnumType.STRING)
  @Column(name = "proficiency", nullable = false)
  private ProficiencyLevel proficiency;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventory_id", nullable = false)
  private TalentInventoryJpa inventory;
}
