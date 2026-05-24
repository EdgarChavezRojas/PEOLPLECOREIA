package com.solveria.core.financial.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: SocialSecurityAccount (Aggregate Root). Tabla: social_security_account. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "social_security_account")
public class SocialSecurityAccountJpa extends BaseEntity {
  @Id
  @Column(name = "ssa_id", nullable = false, unique = true, updatable = false)
  private UUID ssaId;

  @Column(name = "person_id", nullable = false)
  private UUID personId;

  @Column(name = "gestora_code", length = 30, nullable = false)
  private String gestoraCode;

  @Column(name = "contribution_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal contributionRate;

  @Column(name = "last_contribution")
  private LocalDate lastContribution;

  @Column(name = "created_by_user")
  private String createdByUser;
}
