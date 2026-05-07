package com.solveria.TimeAndBearings.domain.model.entity;

import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import com.solveria.TimeAndBearings.domain.model.vo.ExceptionAuditEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Delta calculada entre el turno planificado (AssignedShift de BC-SCH) y la realidad
 * capturada en los TimeEntry. Único documento modificable por el MSS durante WF-TM02.
 *
 * <p>Managed exclusively through {@link AttendanceLedger}.
 * El MSS opera sobre esta entidad; el AR valida que el Ledger no esté CLOSED antes de
 * permitir transiciones de estado (P-TM33).
 *
 * <p>Creación: asíncrona al flujo principal (Non-Blocking Design). Un GEO_VIOLATION o
 * LATE_IN no bloquea la cola de marcaciones; el TimeDeviationRecord se crea después.
 */
public class TimeDeviationRecord {

    private final UUID deviationId;
    private final UUID ledgerId;
    private final DeviationType deviationType;

    /** Duración en minutos. Puede ser negativo para EARLY_OUT. */
    private final int deviationMinutes;

    private ResolutionStatus resolutionStatus;
    private final LocalDateTime detectedAt;

    private LocalDateTime resolvedAt;

    /** FK a User (MSS/Analista). NULL si PENDING o AUTO_CLOSED. */
    private UUID resolvedBy;

    /** Nota obligatoria para APPROVED/OVERRIDDEN (min 20 chars, P-TM32). */
    private String reasonNote;

    /** FK a User. Para retroactividad >48h (P-TM32). */
    private UUID secondaryApproverId;

    /** Historial inmutable de auditoría. Cada transición añade una entrada. */
    private final List<ExceptionAuditEntry> auditTrail = new ArrayList<>();

    /** Package-private: solo el AR puede construir TimeDeviationRecord. */
    public TimeDeviationRecord(
            UUID deviationId,
            UUID ledgerId,
            DeviationType deviationType,
            int deviationMinutes,
            LocalDateTime detectedAt) {

        if (deviationId == null) throw new IllegalStateException("TimeDeviationRecord.deviationId is mandatory.");
        if (ledgerId == null) throw new IllegalStateException("TimeDeviationRecord.ledgerId is mandatory.");
        if (deviationType == null) throw new IllegalStateException("TimeDeviationRecord.deviationType is mandatory.");
        if (detectedAt == null) throw new IllegalStateException("TimeDeviationRecord.detectedAt is mandatory.");

        this.deviationId = deviationId;
        this.ledgerId = ledgerId;
        this.deviationType = deviationType;
        this.deviationMinutes = deviationMinutes;
        this.resolutionStatus = ResolutionStatus.PENDING;
        this.detectedAt = detectedAt;
    }

    /**
     * Resuelve la excepción con la decisión del MSS (WF-TM02).
     * Añade una entrada inmutable al {@code auditTrail}.
     *
     * @param actorId          UUID del MSS o Analista.
     * @param newStatus        Estado resultante (APPROVED, REJECTED, OVERRIDDEN_BY_MANAGER).
     * @param reasonNote       Nota obligatoria (min 20 chars para APPROVED/OVERRIDDEN, P-TM32).
     * @param secondaryApprover UUID del segundo nivel de aprobación si aplica (P-TM32).
     * @param resolvedAt       Momento de la resolución (NTP server time).
     */
    public void resolve(UUID actorId, ResolutionStatus newStatus, String reasonNote,
                        UUID secondaryApprover, LocalDateTime resolvedAt) {

        if (this.resolutionStatus != ResolutionStatus.PENDING) {
            throw new IllegalStateException(
                    "TimeDeviationRecord [" + deviationId + "] is already resolved with status: " + this.resolutionStatus);
        }
        ResolutionStatus previous = this.resolutionStatus;
        // ExceptionAuditEntry validates min reason note length for APPROVED/OVERRIDDEN
        ExceptionAuditEntry auditEntry = new ExceptionAuditEntry(
                actorId,
                resolvedAt.toInstant(java.time.ZoneOffset.UTC),
                previous,
                newStatus,
                reasonNote,
                secondaryApprover);

        this.resolutionStatus = newStatus;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = actorId;
        this.reasonNote = reasonNote;
        this.secondaryApproverId = secondaryApprover;
        this.auditTrail.add(auditEntry);
    }

    /**
     * Auto-cierre por vencimiento de ventana P-TM31.
     * actor=SYSTEM, reason=WINDOW_EXPIRED.
     */
    public void autoClose(LocalDateTime closedAt) {
        if (this.resolutionStatus != ResolutionStatus.PENDING) {
            return; // idempotente
        }
        ResolutionStatus previous = this.resolutionStatus;
        ExceptionAuditEntry auditEntry = new ExceptionAuditEntry(
                null, // SYSTEM actor
                closedAt.toInstant(java.time.ZoneOffset.UTC),
                previous,
                ResolutionStatus.AUTO_CLOSED_AS_UNJUSTIFIED,
                "WINDOW_EXPIRED",
                null);
        this.resolutionStatus = ResolutionStatus.AUTO_CLOSED_AS_UNJUSTIFIED;
        this.resolvedAt = closedAt;
        this.auditTrail.add(auditEntry);
    }

    public boolean isPending() {
        return resolutionStatus == ResolutionStatus.PENDING;
    }

    /**
     * TRUE si esta desviación cubre un MISSING_PUNCH y el MSS la resolvió
     * vía OVERRIDDEN_BY_MANAGER — condición que levanta la Invariante de Paridad de Cierre.
     */
    public boolean coversOpenPunchGap() {
        return deviationType == DeviationType.MISSING_PUNCH
                && resolutionStatus == ResolutionStatus.OVERRIDDEN_BY_MANAGER;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getDeviationId() { return deviationId; }
    public UUID getLedgerId() { return ledgerId; }
    public DeviationType getDeviationType() { return deviationType; }
    public int getDeviationMinutes() { return deviationMinutes; }
    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public UUID getResolvedBy() { return resolvedBy; }
    public String getReasonNote() { return reasonNote; }
    public UUID getSecondaryApproverId() { return secondaryApproverId; }
    public List<ExceptionAuditEntry> getAuditTrail() { return Collections.unmodifiableList(auditTrail); }
}
