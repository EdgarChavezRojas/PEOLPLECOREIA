package com.solveria.scheduling.domain.model.ar;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.entity.TimeEntry;
import com.solveria.scheduling.domain.model.enums.AttendanceStatus;
import com.solveria.scheduling.domain.model.vo.TimeDeviation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

/**
 * Root Entity para agrupar las interacciones de un trabajador en un día específico.
 */
@Getter
public class AttendanceRecord {

    private final UUID recordId;
    private final UUID relationshipId;
    private final LocalDate workDate;
    private boolean isClosed;
    private AttendanceStatus status;
    private final List<TimeEntry> entries = new ArrayList<>();
    private final List<TimeDeviation> deviations = new ArrayList<>();

    public AttendanceRecord(UUID relationshipId, LocalDate workDate) {
        this.recordId = UUID.randomUUID();
        this.relationshipId = relationshipId;
        this.workDate = workDate;
        this.isClosed = false;
        this.status = AttendanceStatus.OPEN;
    }

    /**
     * Constructor completo para reconstrucción desde infraestructura.
     */
    public AttendanceRecord(UUID recordId, UUID relationshipId, LocalDate workDate,
                            boolean isClosed, AttendanceStatus status,
                            List<TimeEntry> entries, List<TimeDeviation> deviations) {
        this.recordId = recordId;
        this.relationshipId = relationshipId;
        this.workDate = workDate;
        this.isClosed = isClosed;
        this.status = status;
        if (entries != null) {
            this.entries.addAll(entries);
        }
        if (deviations != null) {
            this.deviations.addAll(deviations);
        }
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
        this.deviations.add(deviation);
    }

    public void closeRecord() {
        validateClosureInvariants();
        this.isClosed = true;
        this.status = AttendanceStatus.CLOSED;
    }

    public List<TimeEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<TimeDeviation> getDeviations() {
        return Collections.unmodifiableList(deviations);
    }

    private void validateClosureInvariants() {
        if (isClosed) {
            boolean hasEvenEntries = entries.size() % 2 == 0;
            boolean hasApprovedDeviations = deviations.stream()
                .anyMatch(d -> "APPROVED".equals(d.approvalStatus()));

            if (!hasEvenEntries && !hasApprovedDeviations) {
                throw new DomainRuleViolationException("Para cerrar el registro debe haber un número par de marcaciones (IN/OUT exacto) o una desviación (TimeDeviation) aprobada que justifique el hueco.");
            }
        }
    }
}
