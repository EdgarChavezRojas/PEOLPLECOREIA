package com.solveria.scheduling.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sch_attendance_record")
public class AttendanceRecordJpa extends BaseEntity {
  @Id
  @Column(name = "record_id", updatable = false, columnDefinition = "UUID")
  private UUID recordId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "is_closed", nullable = false)
  private boolean isClosed;

  @Column(name = "status", nullable = false)
  private String status;

  @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TimeEntrySchedulingJpa> entries = new ArrayList<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "deviations", columnDefinition = "jsonb")
  private String deviations;

  public UUID getRecordId() {
    return recordId;
  }

  public void setRecordId(UUID recordId) {
    this.recordId = recordId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public void setWorkDate(LocalDate workDate) {
    this.workDate = workDate;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public void setClosed(boolean closed) {
    isClosed = closed;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<TimeEntrySchedulingJpa> getEntries() {
    return entries;
  }

  public void setEntries(List<TimeEntrySchedulingJpa> entries) {
    this.entries = entries;
  }

  public String getDeviations() {
    return deviations;
  }

  public void setDeviations(String deviations) {
    this.deviations = deviations;
  }

  public void addEntry(TimeEntrySchedulingJpa entry) {
    entries.add(entry);
    entry.setAttendanceRecord(this);
  }
}
