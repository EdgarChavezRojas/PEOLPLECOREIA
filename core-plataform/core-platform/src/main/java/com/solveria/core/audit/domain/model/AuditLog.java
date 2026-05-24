package com.solveria.core.audit.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Setter
@Getter
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "action", nullable = false)
  private String action;

  @Column(name = "entity", nullable = false)
  private String entity;

  @Column(name = "entity_id", nullable = false)
  private String entityId;

  /** Actor semántico (NO Long) */
  private String userId;

  private String tenantId;

  private Instant occurredAt;

  protected AuditLog() {}

  public AuditLog(
      String action,
      String entity,
      String entityId,
      String userId,
      String tenantId,
      Instant occurredAt) {
    this.action = action;
    this.entity = entity;
    this.entityId = entityId;
    this.userId = userId;
    this.tenantId = tenantId;
    this.occurredAt = occurredAt;
  }
}
