package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.enums.AuthMethod;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for {@code punch_attempt_log} table — Aggregate 15. Extends {@link BaseEntity}.
 * Append-only — nunca se actualiza un registro ya creado.
 *
 * <p>Registra cada intento de autenticación. Si {@code securityIncident=TRUE}, el sistema emite
 * {@code SecurityPunchIncidentEvent} al Message Broker (P-TM30).
 */
@Entity
@Table(name = "punch_attempt_log")
public class PunchAttemptLogJpa extends BaseEntity {

  @Column(name = "attempt_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID attemptId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "device_id", nullable = false, updatable = false)
  private ClockingDeviceJpa device;

  @Column(name = "attempted_at", nullable = false)
  private LocalDateTime attemptedAt;

  @Column(name = "relationship_id", nullable = false, columnDefinition = "UUID")
  private UUID relationshipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_method", nullable = false, length = 20)
  private AuthMethod authMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_result", nullable = false, length = 30)
  private AuthResult authResult;

  /**
   * TRUE if FRAUD_DETECTED or 3+ consecutive biometric failures (P-TM30). The device is NOT blocked
   * — Non-Blocking Design.
   */
  @Column(name = "security_incident", nullable = false)
  private boolean securityIncident;

  /** FK to User (MSS/Seguridad). NOT NULL when securityIncident=TRUE (async assignment). */
  @Column(name = "incident_escalated_to", columnDefinition = "UUID")
  private UUID incidentEscalatedTo;

  public PunchAttemptLogJpa() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public UUID getAttemptId() {
    return attemptId;
  }

  public void setAttemptId(UUID id) {
    this.attemptId = id;
  }

  public ClockingDeviceJpa getDevice() {
    return device;
  }

  public void setDevice(ClockingDeviceJpa d) {
    this.device = d;
  }

  public LocalDateTime getAttemptedAt() {
    return attemptedAt;
  }

  public void setAttemptedAt(LocalDateTime d) {
    this.attemptedAt = d;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID id) {
    this.relationshipId = id;
  }

  public AuthMethod getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(AuthMethod m) {
    this.authMethod = m;
  }

  public AuthResult getAuthResult() {
    return authResult;
  }

  public void setAuthResult(AuthResult r) {
    this.authResult = r;
  }

  public boolean isSecurityIncident() {
    return securityIncident;
  }

  public void setSecurityIncident(boolean v) {
    this.securityIncident = v;
  }

  public UUID getIncidentEscalatedTo() {
    return incidentEscalatedTo;
  }

  public void setIncidentEscalatedTo(UUID id) {
    this.incidentEscalatedTo = id;
  }
}
