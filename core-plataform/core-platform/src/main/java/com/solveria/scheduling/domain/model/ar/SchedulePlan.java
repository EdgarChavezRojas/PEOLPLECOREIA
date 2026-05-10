package com.solveria.scheduling.domain.model.ar;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.entity.AssignedShift;
import com.solveria.scheduling.domain.model.enums.PlanStatus;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Root Entity para la gestión de la planificación de horarios (Roster Management).
 * Contiene la malla de turnos (AssignedShift).
 */
@Entity
@Table(name = "sch_schedule_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulePlan {

    @Id
    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlanStatus status;

    @Column(name = "total_projected_cost", precision = 15, scale = 2)
    private BigDecimal totalProjectedCost;

    @OneToMany(mappedBy = "schedulePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignedShift> shifts = new ArrayList<>();

    public SchedulePlan(UUID unitId, LocalDate periodStart, LocalDate periodEnd) {
        this.planId = UUID.randomUUID();
        this.unitId = unitId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = PlanStatus.DRAFT;
        this.totalProjectedCost = BigDecimal.ZERO;
        validateInvariants();
    }

    public void publish() {
        // En la vida real, aquí se verifica que no haya hard violations
        this.status = PlanStatus.PUBLISHED;
    }

    public void addShift(AssignedShift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("Shift no puede ser nulo");
        }
        
        // Invariante: Contención temporal
        if (shift.getExpectedStart().toLocalDate().isBefore(this.periodStart) ||
            shift.getExpectedEnd().toLocalDate().isAfter(this.periodEnd)) {
            throw new DomainRuleViolationException("El turno debe estar dentro del periodo del plan");
        }

        // Invariante: No Superposición
        boolean hasOverlap = shifts.stream()
                .filter(s -> s.getRelationshipId().equals(shift.getRelationshipId()) && s.isActive())
                .anyMatch(s -> s.overlapsWith(shift));

        if (hasOverlap) {
            throw new DomainRuleViolationException("No se permiten turnos superpuestos para el mismo relationship_id");
        }

        shifts.add(shift);
        shift.setSchedulePlan(this);
    }

    public void removeShift(AssignedShift shift) {
        shifts.remove(shift);
        shift.setSchedulePlan(null);
    }

    @PrePersist
    @PreUpdate
    private void validateInvariants() {
        if (periodStart == null || periodEnd == null) {
            throw new DomainRuleViolationException("Las fechas de inicio y fin son obligatorias");
        }
        if (!periodEnd.isAfter(periodStart)) {
            throw new DomainRuleViolationException("period_end debe ser mayor a period_start");
        }
    }
}
