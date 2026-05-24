package com.solveria.TimeAndBearings.domain.model.entity;

import com.solveria.TimeAndBearings.domain.model.enums.AuthMethod;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity: PunchAttemptLog — registro de auditoría de cada intento de autenticación en un
 * dispositivo. Diccionario de Datos BC-TM v1.2 – Agregado 15.
 *
 * <p><b>Inmutable por diseño:</b> una vez creado, el log de intento no se modifica. Append-only
 * para garantizar trazabilidad legal.
 *
 * <p><b>Non-Blocking Design:</b> el flag {@code securityIncident=TRUE} registra el fraude pero el
 * dispositivo físico NO se bloquea (WF-TM04 paso 3 / P-TM30).
 *
 * <p>Puro Java 21 — ninguna anotación Spring/JPA.
 */
public class PunchAttemptLog {

  private UUID attemptId;
  private UUID deviceId;
  private LocalDateTime attemptedAt;
  private UUID relationshipId;
  private AuthMethod authMethod;
  private AuthResult authResult;
  private boolean securityIncident;
  private UUID incidentEscalatedTo;

  public void setAttemptId(UUID attemptId) {
    this.attemptId = attemptId;
  }

  public void setDeviceId(UUID deviceId) {
    this.deviceId = deviceId;
  }

  public void setAttemptedAt(LocalDateTime attemptedAt) {
    this.attemptedAt = attemptedAt;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public void setAuthMethod(AuthMethod authMethod) {
    this.authMethod = authMethod;
  }

  public void setAuthResult(AuthResult authResult) {
    this.authResult = authResult;
  }

  public void setSecurityIncident(boolean securityIncident) {
    this.securityIncident = securityIncident;
  }

  public void setIncidentEscalatedTo(UUID incidentEscalatedTo) {
    this.incidentEscalatedTo = incidentEscalatedTo;
  }

  // ── Constructor ──────────────────────────────────────────────────────────

  public PunchAttemptLog(
      UUID attemptId,
      UUID deviceId,
      LocalDateTime attemptedAt,
      UUID relationshipId,
      AuthMethod authMethod,
      AuthResult authResult,
      boolean securityIncident,
      UUID incidentEscalatedTo) {

    this.attemptId = attemptId;
    this.deviceId = deviceId;
    this.attemptedAt = attemptedAt;
    this.relationshipId = relationshipId;
    this.authMethod = authMethod;
    this.authResult = authResult;
    this.securityIncident = securityIncident;
    this.incidentEscalatedTo = incidentEscalatedTo;
  }

  // ── Domain Behavior ──────────────────────────────────────────────────────

  /**
   * Registra el escalado del incidente al rol de seguridad (asíncrono, P-TM30).
   *
   * @param escalatedTo UUID del usuario responsable de seguridad.
   * @throws IllegalStateException si el intento no es un incidente de seguridad.
   */
  public void escalateTo(UUID escalatedTo) {
    if (!this.securityIncident) {
      throw new IllegalStateException(
          "PunchAttemptLog [" + attemptId + "] is not a security incident; cannot escalate.");
    }
    this.incidentEscalatedTo = escalatedTo;
  }

  // ── Queries ──────────────────────────────────────────────────────────────

  public boolean isFraudDetected() {
    return this.authResult == AuthResult.FRAUD_DETECTED;
  }

  // ── Getters ──────────────────────────────────────────────────────────────

  public UUID getAttemptId() {
    return attemptId;
  }

  public UUID getDeviceId() {
    return deviceId;
  }

  public LocalDateTime getAttemptedAt() {
    return attemptedAt;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public AuthMethod getAuthMethod() {
    return authMethod;
  }

  public AuthResult getAuthResult() {
    return authResult;
  }

  public boolean isSecurityIncident() {
    return securityIncident;
  }

  public UUID getIncidentEscalatedTo() {
    return incidentEscalatedTo;
  }
}
