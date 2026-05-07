package com.solveria.core.accruals.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "quinquenio_provision",
    indexes = {
      @Index(name = "idx_quinquenio_relationship", columnList = "relationship_id"),
      @Index(name = "idx_quinquenio_tenant", columnList = "tenant_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuinquenioProvisionJpa {

  @Id
  @Column(name = "provision_id")
  private UUID provisionId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Column(name = "total_accumulated", nullable = false)
  private BigDecimal totalAccumulated;

  @Column(name = "penalty_active", nullable = false)
  private Boolean penaltyActive;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;
}
