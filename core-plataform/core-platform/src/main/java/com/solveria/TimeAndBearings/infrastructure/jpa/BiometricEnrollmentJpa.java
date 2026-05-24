package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.EnrollmentStatus;
import com.solveria.TimeAndBearings.domain.model.enums.RevocationReason;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for {@code biometric_enrollment} table — Aggregate 15. Extends {@link BaseEntity}.
 *
 * <p>El template biométrico raw NUNCA es almacenado. Solo el hash SHA-512 del template normalizado.
 */
@Entity
@Table(
    name = "biometric_enrollment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_enrollment_relationship_type_active",
            columnNames = {"relationship_id", "biometric_type", "status", "tenant_id"}))
public class BiometricEnrollmentJpa extends BaseEntity {
  @Id
  @Column(name = "enrollment_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID enrollmentId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "device_id", nullable = false, updatable = false)
  private ClockingDeviceJpa device;

  @Column(name = "relationship_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID relationshipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "biometric_type", nullable = false, length = 20)
  private BiometricType biometricType;

  /** SHA-512 hash of the normalized biometric template. NEVER the raw image. */
  @Column(name = "template_hash", nullable = false, length = 128)
  private String templateHash;

  /** Quality score of the capture (0.00 – 1.00). */
  @Column(name = "template_quality_score", nullable = false, precision = 4, scale = 2)
  private BigDecimal templateQualityScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private EnrollmentStatus status;

  @Column(name = "enrolled_at", nullable = false)
  private LocalDateTime enrolledAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "revocation_reason", length = 30)
  private RevocationReason revocationReason;

  public BiometricEnrollmentJpa() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public UUID getEnrollmentId() {
    return enrollmentId;
  }

  public void setEnrollmentId(UUID id) {
    this.enrollmentId = id;
  }

  public ClockingDeviceJpa getDevice() {
    return device;
  }

  public void setDevice(ClockingDeviceJpa d) {
    this.device = d;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID id) {
    this.relationshipId = id;
  }

  public BiometricType getBiometricType() {
    return biometricType;
  }

  public void setBiometricType(BiometricType t) {
    this.biometricType = t;
  }

  public String getTemplateHash() {
    return templateHash;
  }

  public void setTemplateHash(String h) {
    this.templateHash = h;
  }

  public BigDecimal getTemplateQualityScore() {
    return templateQualityScore;
  }

  public void setTemplateQualityScore(BigDecimal v) {
    this.templateQualityScore = v;
  }

  public EnrollmentStatus getStatus() {
    return status;
  }

  public void setStatus(EnrollmentStatus s) {
    this.status = s;
  }

  public LocalDateTime getEnrolledAt() {
    return enrolledAt;
  }

  public void setEnrolledAt(LocalDateTime d) {
    this.enrolledAt = d;
  }

  public LocalDateTime getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(LocalDateTime d) {
    this.revokedAt = d;
  }

  public RevocationReason getRevocationReason() {
    return revocationReason;
  }

  public void setRevocationReason(RevocationReason r) {
    this.revocationReason = r;
  }
}
