package com.solveria.scheduling.domain.model.entity;

import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.domain.model.enums.ShiftType;
import com.solveria.scheduling.domain.model.vo.ConstraintViolation;
import com.solveria.scheduling.domain.model.vo.ShiftMetadata;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entidad que representa un bloque de tiempo asignado a un trabajador (Relationship).
 */
@Entity
@Table(name = "sch_assigned_shift")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignedShift {

    @Id
    @Column(name = "shift_id")
    private UUID shiftId;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SchedulePlan schedulePlan;

    @Column(name = "relationship_id", nullable = false)
    private UUID relationshipId;

    @Column(name = "expected_start", nullable = false)
    private LocalDateTime expectedStart;

    @Column(name = "expected_end", nullable = false)
    private LocalDateTime expectedEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private ShiftMetadata metadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "violations", columnDefinition = "jsonb")
    private List<ConstraintViolation> violations = new ArrayList<>();

    public AssignedShift(UUID relationshipId, LocalDateTime expectedStart, LocalDateTime expectedEnd, ShiftType shiftType) {
        this.shiftId = UUID.randomUUID();
        this.relationshipId = relationshipId;
        this.expectedStart = expectedStart;
        this.expectedEnd = expectedEnd;
        this.shiftType = shiftType;
        this.isActive = true;
    }

    public void updateMetadata(ShiftMetadata metadata) {
        this.metadata = metadata;
    }

    public void addViolation(ConstraintViolation violation) {
        if (this.violations == null) {
            this.violations = new ArrayList<>();
        }
        this.violations.add(violation);
    }

    public void cancelShift() {
        this.isActive = false;
    }

    public boolean overlapsWith(AssignedShift other) {
        if (other == null) return false;
        return this.expectedStart.isBefore(other.expectedEnd) && other.expectedStart.isBefore(this.expectedEnd);
    }
}
