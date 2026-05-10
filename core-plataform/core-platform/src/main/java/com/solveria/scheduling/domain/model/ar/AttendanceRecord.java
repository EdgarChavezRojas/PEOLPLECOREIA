package com.solveria.scheduling.domain.model.ar;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.entity.TimeEntry;
import com.solveria.scheduling.domain.model.enums.AttendanceStatus;
import com.solveria.scheduling.domain.model.vo.TimeDeviation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Root Entity para agrupar las interacciones de un trabajador en un día específico.
 */
@Entity
@Table(name = "sch_attendance_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord {

    @Id
    @Column(name = "record_id")
    private UUID recordId;

    @Column(name = "relationship_id", nullable = false)
    private UUID relationshipId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "is_closed", nullable = false)
    private boolean isClosed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> entries = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deviations", columnDefinition = "jsonb")
    private List<TimeDeviation> deviations = new ArrayList<>();

    public AttendanceRecord(UUID relationshipId, LocalDate workDate) {
        this.recordId = UUID.randomUUID();
        this.relationshipId = relationshipId;
        this.workDate = workDate;
        this.isClosed = false;
        this.status = AttendanceStatus.OPEN;
    }

    public void addEntry(TimeEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("TimeEntry no puede ser nulo");
        }
        if (this.isClosed) {
            throw new DomainRuleViolationException("No se pueden añadir marcaciones a un registro cerrado");
        }

        // Invariante de orden cronológico (No Time-Travel de eventos previos)
        if (!entries.isEmpty()) {
            TimeEntry lastEntry = entries.stream()
                .max(Comparator.comparing(TimeEntry::getPunchTime))
                .orElse(null);
            
            if (lastEntry != null && entry.getPunchTime().isBefore(lastEntry.getPunchTime())) {
                throw new DomainRuleViolationException("Las marcaciones deben ser cronológicas. La nueva marcación es anterior a la última registrada.");
            }
        }

        entries.add(entry);
        entry.setAttendanceRecord(this);
    }

    public void addDeviation(TimeDeviation deviation) {
        if (this.deviations == null) {
            this.deviations = new ArrayList<>();
        }
        this.deviations.add(deviation);
    }

    public void closeRecord() {
        validateClosureInvariants();
        this.isClosed = true;
        this.status = AttendanceStatus.CLOSED;
    }

    @PrePersist
    @PreUpdate
    private void validateClosureInvariants() {
        if (isClosed) {
            boolean hasEvenEntries = entries.size() % 2 == 0;
            boolean hasApprovedDeviations = deviations != null && deviations.stream()
                .anyMatch(d -> "APPROVED".equals(d.approvalStatus()));

            if (!hasEvenEntries && !hasApprovedDeviations) {
                throw new DomainRuleViolationException("Para cerrar el registro debe haber un número par de marcaciones (IN/OUT exacto) o una desviación (TimeDeviation) aprobada que justifique el hueco.");
            }
        }
    }
}
