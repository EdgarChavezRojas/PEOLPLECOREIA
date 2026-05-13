package com.solveria.scheduling.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sch_time_entry")
public class TimeEntryJpa extends BaseEntity {

    @Column(name = "entry_id", updatable = false, columnDefinition = "UUID")
    private UUID entryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private AttendanceRecordJpa attendanceRecord;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Column(name = "punch_type", nullable = false)
    private String punchType;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "source")
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geo_validation", columnDefinition = "jsonb")
    private String geoValidation;

    public UUID getEntryId() { return entryId; }
    public void setEntryId(UUID entryId) { this.entryId = entryId; }

    public AttendanceRecordJpa getAttendanceRecord() { return attendanceRecord; }
    public void setAttendanceRecord(AttendanceRecordJpa attendanceRecord) { this.attendanceRecord = attendanceRecord; }

    public LocalDateTime getPunchTime() { return punchTime; }
    public void setPunchTime(LocalDateTime punchTime) { this.punchTime = punchTime; }

    public String getPunchType() { return punchType; }
    public void setPunchType(String punchType) { this.punchType = punchType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getGeoValidation() { return geoValidation; }
    public void setGeoValidation(String geoValidation) { this.geoValidation = geoValidation; }
}
