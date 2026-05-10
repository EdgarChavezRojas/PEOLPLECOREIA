package com.solveria.scheduling.domain.model.entity;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.domain.model.enums.PunchType;
import com.solveria.scheduling.domain.model.vo.GeoValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entidad que representa un evento atómico de marcación (Punch).
 */
@Entity
@Table(name = "sch_time_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeEntry {

    @Id
    @Column(name = "entry_id")
    private UUID entryId;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private AttendanceRecord attendanceRecord;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false)
    private PunchType punchType;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "source")
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geo_validation", columnDefinition = "jsonb")
    private GeoValidation geoValidation;

    public TimeEntry(LocalDateTime punchTime, PunchType punchType, String deviceId, String source, GeoValidation geoValidation) {
        this.entryId = UUID.randomUUID();
        this.punchTime = punchTime;
        this.punchType = punchType;
        this.deviceId = deviceId;
        this.source = source;
        this.geoValidation = geoValidation;
    }

    @PrePersist
    private void validateNoTimeTravel() {
        // Invariante (No Time-Travel): No se permiten horas en el futuro relativo al servidor
        if (punchTime.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1))) { // 1 min buffer
            throw new DomainRuleViolationException("La hora de marcación no puede estar en el futuro.");
        }
    }
}
