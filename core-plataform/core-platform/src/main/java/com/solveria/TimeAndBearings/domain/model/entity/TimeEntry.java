package com.solveria.TimeAndBearings.domain.model.entity;

import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.TimeAndBearings.domain.model.enums.PunchType;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento atómico e inmutable de marcación. NUNCA se modifica.
 * Las correcciones crean un nuevo TimeEntry con {@code punchType = MANUAL_CORRECTION}
 * y {@code correctsEntryId} apuntando al original (P-TM32).
 *
 * <p>Managed exclusively through {@link AttendanceLedger}
 * (Aggregate Root). No public setters.
 *
 * <p>Invariantes aplicadas externamente por el AR antes de añadir esta entidad:
 * <ul>
 *   <li>Chronological Integrity (punch_time ≤ server NTP + 5s tolerance, P-TM26).</li>
 *   <li>Active Punch Uniqueness (solo un PUNCH_IN activo por ledger).</li>
 *   <li>Device Signature Integrity (KIOSK/BIOMETRIC_READER requieren deviceSignature, Invariante Agg 14).</li>
 * </ul>
 */
public class TimeEntry {

    private UUID entryId;
    private UUID ledgerId;
    private LocalDateTime punchTime;
    private PunchType punchType;
    private PunchContext punchContext;
    private GeoValidationSnapshot geoSnapshot;
    private String deviceSignature;
    private boolean isRetroactive;
    private UUID retroactiveApproverId;
    private UUID correctsEntryId;
    private boolean fraudFlag;

    public void setEntryId(UUID entryId) { this.entryId = entryId; }
    public void setLedgerId(UUID ledgerId) { this.ledgerId = ledgerId; }
    public void setPunchTime(LocalDateTime punchTime) { this.punchTime = punchTime; }
    public void setPunchType(PunchType punchType) { this.punchType = punchType; }
    public void setPunchContext(PunchContext punchContext) { this.punchContext = punchContext; }
    public void setGeoSnapshot(GeoValidationSnapshot geoSnapshot) { this.geoSnapshot = geoSnapshot; }
    public void setDeviceSignature(String deviceSignature) { this.deviceSignature = deviceSignature; }
    public void setIsRetroactive(boolean isRetroactive) { this.isRetroactive = isRetroactive; }
    public void setRetroactiveApproverId(UUID retroactiveApproverId) { this.retroactiveApproverId = retroactiveApproverId; }
    public void setCorrectsEntryId(UUID correctsEntryId) { this.correctsEntryId = correctsEntryId; }
    public void setFraudFlag(boolean fraudFlag) { this.fraudFlag = fraudFlag; }

    /** Package-private: solo el AR puede construir TimeEntry. */
    public TimeEntry(
            UUID entryId,
            UUID ledgerId,
            LocalDateTime punchTime,
            PunchType punchType,
            PunchContext punchContext,
            GeoValidationSnapshot geoSnapshot,
            String deviceSignature,
            boolean isRetroactive,
            UUID retroactiveApproverId,
            UUID correctsEntryId,
            boolean fraudFlag) {

        this.entryId = entryId;
        this.ledgerId = ledgerId;
        this.punchTime = punchTime;
        this.punchType = punchType;
        this.punchContext = punchContext;
        this.geoSnapshot = geoSnapshot;
        this.deviceSignature = deviceSignature;
        this.isRetroactive = isRetroactive;
        this.retroactiveApproverId = retroactiveApproverId;
        this.correctsEntryId = correctsEntryId;
        this.fraudFlag = fraudFlag;

        validate();
    }

    private void validate() {
        if (entryId == null) throw new IllegalStateException("TimeEntry.entryId is mandatory.");
        if (ledgerId == null) throw new IllegalStateException("TimeEntry.ledgerId is mandatory.");
        if (punchTime == null) throw new IllegalStateException("TimeEntry.punchTime is mandatory (NTP server time, P-TM26).");
        if (punchType == null) throw new IllegalStateException("TimeEntry.punchType is mandatory.");
        if (punchContext == null) throw new IllegalStateException("TimeEntry.punchContext is mandatory.");

        // Invariante Device Signature Integrity
        PunchSource source = punchContext.sourceChannel();
        if ((source == PunchSource.KIOSK || source == PunchSource.BIOMETRIC_READER)
                && (deviceSignature == null || deviceSignature.isBlank())) {
            throw new IllegalStateException(
                    "TimeEntry: deviceSignature is mandatory for KIOSK/BIOMETRIC_READER channels (Invariante Device Signature Integrity).");
        }

        // P-TM32: retroactive entry requires approver
        if (isRetroactive && retroactiveApproverId == null) {
            throw new IllegalStateException(
                    "TimeEntry: retroactiveApproverId is mandatory when isRetroactive=TRUE (P-TM32).");
        }
    }

    // ── Getters (immutable) ─────────────────────────────────────────────────────

    public UUID getEntryId() { return entryId; }
    public UUID getLedgerId() { return ledgerId; }
    public LocalDateTime getPunchTime() { return punchTime; }
    public PunchType getPunchType() { return punchType; }
    public PunchContext getPunchContext() { return punchContext; }
    public GeoValidationSnapshot getGeoSnapshot() { return geoSnapshot; }
    public String getDeviceSignature() { return deviceSignature; }
    public boolean isRetroactive() { return isRetroactive; }
    public UUID getRetroactiveApproverId() { return retroactiveApproverId; }
    public UUID getCorrectsEntryId() { return correctsEntryId; }
    public boolean isFraudFlag() { return fraudFlag; }

    /** Convenience: returns true when this entry is a PUNCH_IN and has no semantic PUNCH_OUT pairing yet. */
    public boolean isPunchIn() {
        return punchType == PunchType.PUNCH_IN;
    }
}
