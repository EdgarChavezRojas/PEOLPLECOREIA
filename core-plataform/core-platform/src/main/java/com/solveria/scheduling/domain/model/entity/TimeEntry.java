package com.solveria.scheduling.domain.model.entity;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.domain.model.enums.PunchType;
import com.solveria.scheduling.domain.model.vo.GeoValidation;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Getter;

/**
 * Entidad que representa un evento atómico de marcación (Punch).
 */
@Getter
public class TimeEntry {

    private final UUID entryId;
    private AttendanceRecord attendanceRecord;
    private final LocalDateTime punchTime;
    private final PunchType punchType;
    private final String deviceId;
    private final String source;
    private final GeoValidation geoValidation;

    public TimeEntry(LocalDateTime punchTime, PunchType punchType, String deviceId, String source, GeoValidation geoValidation) {
        this.entryId = UUID.randomUUID();
        this.punchTime = punchTime;
        this.punchType = punchType;
        this.deviceId = deviceId;
        this.source = source;
        this.geoValidation = geoValidation;
        validateNoTimeTravel();
    }

    /**
     * Constructor completo para reconstrucción desde infraestructura.
     */
    public TimeEntry(UUID entryId, LocalDateTime punchTime, PunchType punchType,
                     String deviceId, String source, GeoValidation geoValidation) {
        this.entryId = entryId;
        this.punchTime = punchTime;
        this.punchType = punchType;
        this.deviceId = deviceId;
        this.source = source;
        this.geoValidation = geoValidation;
    }

    public void setAttendanceRecord(AttendanceRecord attendanceRecord) {
        this.attendanceRecord = attendanceRecord;
    }

    private void validateNoTimeTravel() {
        // Invariante (No Time-Travel): No se permiten horas en el futuro relativo al servidor
        if (punchTime.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1))) { // 1 min buffer
            throw new DomainRuleViolationException("La hora de marcación no puede estar en el futuro.");
        }
    }
}
