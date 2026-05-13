package com.solveria.scheduling.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sch_schedule_plan")
public class SchedulePlanJpa extends BaseEntity {

    @Column(name = "plan_id", updatable = false, columnDefinition = "UUID")
    private UUID planId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "total_projected_cost", precision = 15, scale = 2)
    private BigDecimal totalProjectedCost;

    @OneToMany(mappedBy = "schedulePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignedShiftJpa> shifts = new ArrayList<>();

    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }

    public UUID getUnitId() { return unitId; }
    public void setUnitId(UUID unitId) { this.unitId = unitId; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalProjectedCost() { return totalProjectedCost; }
    public void setTotalProjectedCost(BigDecimal totalProjectedCost) { this.totalProjectedCost = totalProjectedCost; }

    public List<AssignedShiftJpa> getShifts() { return shifts; }
    public void setShifts(List<AssignedShiftJpa> shifts) { this.shifts = shifts; }

    public void addShift(AssignedShiftJpa shift) {
        shifts.add(shift);
        shift.setSchedulePlan(this);
    }
}
