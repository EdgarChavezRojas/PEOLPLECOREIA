package com.solveria.scheduling.infrastructure.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.domain.model.entity.AssignedShift;
import com.solveria.scheduling.domain.model.enums.PlanStatus;
import com.solveria.scheduling.domain.model.enums.ShiftType;
import com.solveria.scheduling.domain.model.vo.ConstraintViolation;
import com.solveria.scheduling.domain.model.vo.ShiftMetadata;
import com.solveria.scheduling.infrastructure.jpa.AssignedShiftJpa;
import com.solveria.scheduling.infrastructure.jpa.SchedulePlanJpa;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para convertir entre SchedulePlan (dominio) y SchedulePlanJpa (infraestructura).
 */
@Component
public class SchedulePlanMapper {

    private final ObjectMapper objectMapper;

    public SchedulePlanMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SchedulePlanJpa toJpa(SchedulePlan domain) {
        if (domain == null) return null;

        SchedulePlanJpa jpa = new SchedulePlanJpa();
        jpa.setPlanId(domain.getPlanId());
        jpa.setUnitId(domain.getUnitId());
        jpa.setPeriodStart(domain.getPeriodStart());
        jpa.setPeriodEnd(domain.getPeriodEnd());
        jpa.setStatus(domain.getStatus().name());
        jpa.setTotalProjectedCost(domain.getTotalProjectedCost());

        List<AssignedShiftJpa> shiftJpas = domain.getShifts().stream()
            .map(shift -> toAssignedShiftJpa(shift, jpa))
            .collect(Collectors.toList());
        jpa.setShifts(shiftJpas);

        return jpa;
    }

    public SchedulePlan toDomain(SchedulePlanJpa jpa) {
        if (jpa == null) return null;

        List<AssignedShift> shifts = jpa.getShifts().stream()
            .map(this::toAssignedShiftDomain)
            .collect(Collectors.toList());

        return new SchedulePlan(
            jpa.getPlanId(),
            jpa.getUnitId(),
            jpa.getPeriodStart(),
            jpa.getPeriodEnd(),
            PlanStatus.valueOf(jpa.getStatus()),
            jpa.getTotalProjectedCost(),
            shifts
        );
    }

    private AssignedShiftJpa toAssignedShiftJpa(AssignedShift shift, SchedulePlanJpa parentJpa) {
        AssignedShiftJpa jpa = new AssignedShiftJpa();
        jpa.setShiftId(shift.getShiftId());
        jpa.setSchedulePlan(parentJpa);
        jpa.setRelationshipId(shift.getRelationshipId());
        jpa.setExpectedStart(shift.getExpectedStart());
        jpa.setExpectedEnd(shift.getExpectedEnd());
        jpa.setShiftType(shift.getShiftType().name());
        jpa.setActive(shift.isActive());
        jpa.setMetadata(toJson(shift.getMetadata()));
        jpa.setViolations(toJson(shift.getViolations()));
        return jpa;
    }

    private AssignedShift toAssignedShiftDomain(AssignedShiftJpa jpa) {
        ShiftMetadata metadata = fromJson(jpa.getMetadata(), new TypeReference<>() {});
        List<ConstraintViolation> violations = fromJson(jpa.getViolations(), new TypeReference<>() {});

        return new AssignedShift(
            jpa.getShiftId(),
            jpa.getRelationshipId(),
            jpa.getExpectedStart(),
            jpa.getExpectedEnd(),
            ShiftType.valueOf(jpa.getShiftType()),
            jpa.isActive(),
            metadata,
            violations
        );
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error serializando a JSON", e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error deserializando desde JSON", e);
        }
    }
}
