package com.solveria.scheduling.domain.service;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.entity.AssignedShift;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de dominio para validar políticas de asignación de turnos (Rostering). Implementa las
 * políticas P20 y P24.
 */
@Component
public class RosterValidationService {

  /**
   * P20 (Anti-Clopening): Hard Constraint. Bloquea turnos si no hay mínimo 12 horas continuas de
   * descanso entre el expected_end de un turno y el expected_start del siguiente.
   *
   * @param existingShifts Turnos existentes para el relationship_id
   * @param newShift Nuevo turno a asignar
   */
  public void validateAntiClopening(List<AssignedShift> existingShifts, AssignedShift newShift) {
    for (AssignedShift existing : existingShifts) {
      if (!existing.isActive()) continue;

      // Verificar la brecha entre el turno existente y el nuevo turno
      if (existing.getExpectedEnd().isBefore(newShift.getExpectedStart())) {
        long hoursBetween =
            Duration.between(existing.getExpectedEnd(), newShift.getExpectedStart()).toHours();
        if (hoursBetween < 12) {
          throw new DomainRuleViolationException(
              "P20 Anti-Clopening: Se requieren mínimo 12 horas de descanso entre turnos.");
        }
      } else if (newShift.getExpectedEnd().isBefore(existing.getExpectedStart())) {
        long hoursBetween =
            Duration.between(newShift.getExpectedEnd(), existing.getExpectedStart()).toHours();
        if (hoursBetween < 12) {
          throw new DomainRuleViolationException(
              "P20 Anti-Clopening: Se requieren mínimo 12 horas de descanso entre turnos.");
        }
      }
    }
  }

  /**
   * P24 (Límite Días Consecutivos): Hard Constraint. Bloquea asignación si acumula 6 días
   * trabajados consecutivos.
   *
   * @param existingShifts Turnos existentes en el mes/periodo para el relationship_id
   * @param newShift Nuevo turno a asignar
   */
  public void validateConsecutiveDaysLimit(
      List<AssignedShift> existingShifts, AssignedShift newShift) {
    // Obtenemos todos los días en los que hay turno activo, incluyendo el nuevo
    List<LocalDate> workDays =
        new java.util.ArrayList<>(
            existingShifts.stream()
                .filter(AssignedShift::isActive)
                .map(s -> s.getExpectedStart().toLocalDate())
                .distinct()
                .toList());

    LocalDate newShiftDay = newShift.getExpectedStart().toLocalDate();
    if (!workDays.contains(newShiftDay)) {
      workDays.add(newShiftDay);
    }

    workDays.sort(Comparator.naturalOrder());

    int consecutiveCount = 1;
    for (int i = 1; i < workDays.size(); i++) {
      if (workDays.get(i).minusDays(1).equals(workDays.get(i - 1))) {
        consecutiveCount++;
        if (consecutiveCount >= 6) {
          throw new DomainRuleViolationException(
              "P24 Límite Días Consecutivos: No se pueden asignar 6 o más días de trabajo consecutivos.");
        }
      } else {
        consecutiveCount = 1;
      }
    }
  }
}
