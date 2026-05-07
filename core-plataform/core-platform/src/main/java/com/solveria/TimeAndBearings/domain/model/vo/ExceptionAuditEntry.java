package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Registro inmutable de cada acción tomada sobre un TimeDeviationRecord.
 * Value Object – Aggregate 14: AttendanceLedger.
 *
 * <p>Cada transición de estado del TimeDeviationRecord genera una nueva entrada.
 * Para cierres automáticos por vencimiento (P-TM31): actor=SYSTEM, reasonNote=WINDOW_EXPIRED.
 *
 * @param actorId             UUID del usuario (MSS/Analista) o "SYSTEM" para auto-cierres.
 * @param timestamp           Momento exacto de la acción (NTP server time).
 * @param oldStatus           Estado anterior del TimeDeviationRecord.
 * @param newStatus           Estado resultante de la acción.
 * @param reasonNote          Nota obligatoria del MSS (mínimo 20 chars para APPROVED/OVERRIDDEN per P-TM32).
 * @param secondaryApproverId UUID del segundo nivel de aprobación requerido para retroactividad >48h (P-TM32).
 */
public record ExceptionAuditEntry(
        UUID actorId,
        Instant timestamp,
        ResolutionStatus oldStatus,
        ResolutionStatus newStatus,
        String reasonNote,
        UUID secondaryApproverId
) {

    private static final int MIN_REASON_NOTE_LENGTH = 20;

    public ExceptionAuditEntry {
        if (timestamp == null) {
            throw new IllegalArgumentException("ExceptionAuditEntry: timestamp is mandatory.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("ExceptionAuditEntry: newStatus is mandatory.");
        }
        if ((newStatus == ResolutionStatus.APPROVED || newStatus == ResolutionStatus.OVERRIDDEN_BY_MANAGER)
                && (reasonNote == null || reasonNote.trim().length() < MIN_REASON_NOTE_LENGTH)) {
            throw new IllegalArgumentException(
                    "ExceptionAuditEntry: reasonNote must have at least " + MIN_REASON_NOTE_LENGTH +
                    " characters for APPROVED or OVERRIDDEN_BY_MANAGER status (P-TM32).");
        }
    }
}
