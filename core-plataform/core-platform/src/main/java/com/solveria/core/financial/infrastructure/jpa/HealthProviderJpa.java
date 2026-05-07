package com.solveria.core.financial.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: HealthProvider (Caja Nacional de Salud, etc.). Tabla: health_provider. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "health_provider")
public class HealthProviderJpa extends BaseEntity {

  @Column(name = "provider_id", nullable = false, unique = true, updatable = false)
  private UUID providerId;

  @Column(name = "registration_no", length = 50, nullable = false)
  private String registrationNo;

  @Column(name = "status", length = 20, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.solveria.core.financial.domain.model.vo.HealthProviderStatus status;

  @Column(name = "created_by_user")
  private String createdByUser;
}
