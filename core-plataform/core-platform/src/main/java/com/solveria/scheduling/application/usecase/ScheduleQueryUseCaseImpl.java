package com.solveria.scheduling.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.scheduling.application.dto.response.AssignedShiftResponseDto;
import com.solveria.scheduling.application.dto.response.ScheduleEmployeeResponseDto;
import com.solveria.scheduling.application.port.inbound.ScheduleQueryUseCase;
import com.solveria.scheduling.application.port.outbound.CoreHrPort;
import com.solveria.scheduling.domain.exception.EmployeeNotActiveException;
import com.solveria.scheduling.infrastructure.jpa.AssignedShiftJpa;
import com.solveria.scheduling.infrastructure.repository.AssignedShiftJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso (Read Model / CQRS) enfocado en la consulta ágil de turnos asignados de empleados.
 */
@Service
@RequiredArgsConstructor
public class ScheduleQueryUseCaseImpl implements ScheduleQueryUseCase {

  private final AssignedShiftJpaRepository assignedShiftJpaRepository;
  private final CoreHrPort coreHrPort;

  @Override
  @Transactional(readOnly = true)
  public ScheduleEmployeeResponseDto getEmployeeSchedule(
      UUID relationshipId, LocalDate startDate, LocalDate endDate) {

    // 1. Obtener tenantId desde el contexto de seguridad para aislamiento multi-tenant
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    // 2. Validar que el empleado exista y esté ACTIVE en Core HR (BC 01)
    if (!coreHrPort.isEmployeeActive(relationshipId)) {
      throw new EmployeeNotActiveException(relationshipId);
    }

    // 3. Convertir rangos de fecha LocalDate a límites LocalDateTime para la consulta en DB
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    // 4. Ejecutar consulta de lectura directa sobre la tabla sch_assigned_shift
    List<AssignedShiftJpa> shifts =
        assignedShiftJpaRepository.findByRelationshipIdAndDateRange(
            relationshipId, tenantId, startDateTime, endDateTime);

    // 5. Mapear a DTOs de respuesta para evitar acoplamiento con entidades JPA/Dominio
    List<AssignedShiftResponseDto> shiftDtos =
        shifts.stream().map(this::mapToAssignedShiftDto).toList();

    return ScheduleEmployeeResponseDto.builder()
        .relationshipId(relationshipId)
        .startDate(startDate)
        .endDate(endDate)
        .shifts(shiftDtos)
        .build();
  }

  private AssignedShiftResponseDto mapToAssignedShiftDto(AssignedShiftJpa jpa) {
    return AssignedShiftResponseDto.builder()
        .shiftId(jpa.getShiftId())
        .relationshipId(jpa.getRelationshipId())
        .expectedStart(jpa.getExpectedStart())
        .expectedEnd(jpa.getExpectedEnd())
        .shiftType(jpa.getShiftType())
        .isActive(jpa.isActive())
        .metadata(jpa.getMetadata())
        .violations(jpa.getViolations())
        .build();
  }
}
