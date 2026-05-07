package com.solveria.TimeAndBearings.domain.model.entity;

import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.EnrollmentStatus;
import com.solveria.TimeAndBearings.domain.model.enums.RevocationReason;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity: BiometricEnrollment — vincula el template biométrico (hash) de un colaborador con un ClockingDevice.
 * Diccionario de Datos BC-TM v1.2 – Agregado 15.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Garantizar que solo exista un template {@code ACTIVE} por (relationship_id, biometric_type)
 *       dentro de un ClockingDevice — invariante gestionada por el AR {@code ClockingDevice}.</li>
 *   <li>Gestionar la revocación automática cuando se recibe el evento {@code EMPLOYEE_DEACTIVATED}
 *       (WF-TM04 Flujo Baja, paso 1A).</li>
 * </ul>
 *
 * <p><b>Template Storage Rule (P-TM30 / WF-TM04):</b>
 * El campo {@code templateHash} contiene el hash SHA-512 del template normalizado.
 * La imagen biométrica raw NUNCA es almacenada en el sistema.
 *
 * <p>Puro Java 21 — ninguna anotación Spring/JPA.
 */
public class BiometricEnrollment {

    private final UUID enrollmentId;
    private final UUID deviceId;
    private final UUID relationshipId;
    private final BiometricType biometricType;

    /** SHA-512 del template normalizado. NUNCA la imagen raw (WF-TM04 paso 2). */
    private final String templateHash;

    /** Score de calidad de la captura biométrica (0.00 – 1.00). */
    private final BigDecimal templateQualityScore;

    private EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private LocalDateTime revokedAt;
    private RevocationReason revocationReason;

    // ── Constructor: creación de nuevo enrollment ────────────────────────────

    /**
     * Crea un nuevo BiometricEnrollment en estado ACTIVE.
     * Llamado por {@link ClockingDevice#enrollBiometric}
     * después de validar la unicidad.
     */
    public BiometricEnrollment(
            UUID enrollmentId,
            UUID deviceId,
            UUID relationshipId,
            BiometricType biometricType,
            String templateHash,
            BigDecimal templateQualityScore,
            LocalDateTime enrolledAt) {

        if (templateHash == null || templateHash.isBlank()) {
            throw new IllegalArgumentException("templateHash is mandatory for BiometricEnrollment.");
        }
        if (templateQualityScore == null
                || templateQualityScore.compareTo(BigDecimal.ZERO) < 0
                || templateQualityScore.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "templateQualityScore must be in range [0.00, 1.00]. Got: " + templateQualityScore);
        }

        this.enrollmentId = enrollmentId;
        this.deviceId = deviceId;
        this.relationshipId = relationshipId;
        this.biometricType = biometricType;
        this.templateHash = templateHash;
        this.templateQualityScore = templateQualityScore;
        this.status = EnrollmentStatus.ACTIVE;
        this.enrolledAt = enrolledAt;
        this.revokedAt = null;
        this.revocationReason = null;
    }

    /** Reconstitution constructor (usado por el repository adapter). */
    public BiometricEnrollment(
            UUID enrollmentId,
            UUID deviceId,
            UUID relationshipId,
            BiometricType biometricType,
            String templateHash,
            BigDecimal templateQualityScore,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime revokedAt,
            RevocationReason revocationReason) {

        this.enrollmentId = enrollmentId;
        this.deviceId = deviceId;
        this.relationshipId = relationshipId;
        this.biometricType = biometricType;
        this.templateHash = templateHash;
        this.templateQualityScore = templateQualityScore;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.revokedAt = revokedAt;
        this.revocationReason = revocationReason;
    }

    // ── Domain Behavior ──────────────────────────────────────────────────────

    /**
     * Revoca el enrollment (WF-TM04 Flujo Baja / paso 2).
     * El dispositivo recibirá señal de sincronización para eliminar el template local.
     *
     * @param revokedAt        Hora del servidor NTP de la revocación.
     * @param revocationReason Razón de la revocación.
     * @throws IllegalStateException si el enrollment ya está REVOKED.
     */
    public void revoke(LocalDateTime revokedAt, RevocationReason revocationReason) {
        if (this.status == EnrollmentStatus.REVOKED) {
            throw new IllegalStateException(
                    "BiometricEnrollment [" + enrollmentId + "] is already REVOKED.");
        }
        this.status = EnrollmentStatus.REVOKED;
        this.revokedAt = revokedAt;
        this.revocationReason = revocationReason;
    }

    /** Suspende temporalmente el enrollment (ej. investigación de fraude). */
    public void suspend() {
        if (this.status == EnrollmentStatus.REVOKED) {
            throw new IllegalStateException(
                    "Cannot suspend a REVOKED BiometricEnrollment [" + enrollmentId + "].");
        }
        this.status = EnrollmentStatus.SUSPENDED;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getEnrollmentId()          { return enrollmentId; }
    public UUID getDeviceId()              { return deviceId; }
    public UUID getRelationshipId()        { return relationshipId; }
    public BiometricType getBiometricType() { return biometricType; }
    public String getTemplateHash()        { return templateHash; }
    public BigDecimal getTemplateQualityScore() { return templateQualityScore; }
    public EnrollmentStatus getStatus()    { return status; }
    public LocalDateTime getEnrolledAt()   { return enrolledAt; }
    public LocalDateTime getRevokedAt()    { return revokedAt; }
    public RevocationReason getRevocationReason() { return revocationReason; }
}
