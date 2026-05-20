package com.solveria.scheduling.domain.service;

import com.solveria.scheduling.domain.model.entity.AssignedShift;
import com.solveria.scheduling.domain.model.enums.DeviationType;
import com.solveria.scheduling.domain.model.vo.TimeDeviation;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Servicio de dominio para validar las políticas de marcación y tiempo. Implementa las políticas
 * P19 y P22.
 */
@Component
public class TimeTrackingValidationService {

  private static final int TOLERANCE_MINUTES = 10;
  private static final int OVERTIME_THRESHOLD_MINUTES = 15;

  /**
   * P19 (Tolerancia y Redondeo): Tolerancia de 5-10 minutos para Punch-In. Solo genera estado
   * LATE_IN si supera tolerancia.
   *
   * @param shift Turno planificado
   * @param punchInTime Hora real de la marcación de entrada
   * @return TimeDeviation si hay retraso fuera de tolerancia, null caso contrario.
   */
  public TimeDeviation calculateLateIn(AssignedShift shift, LocalDateTime punchInTime) {
    if (punchInTime.isAfter(shift.getExpectedStart())) {
      long lateMinutes = Duration.between(shift.getExpectedStart(), punchInTime).toMinutes();
      if (lateMinutes > TOLERANCE_MINUTES) {
        // Redondeo al cuarto de hora más cercano
        int roundedMinutes = (int) (Math.round(lateMinutes / 15.0) * 15);
        return new TimeDeviation(DeviationType.LATE_IN, roundedMinutes, "PENDING");
      }
    }
    return null;
  }

  /**
   * P22 (Strict Overtime): Tiempo excedente > 15 mins tras el fin de turno oficial se marca como
   * PENDING_OVERTIME.
   *
   * @param shift Turno planificado
   * @param punchOutTime Hora real de la marcación de salida
   * @return TimeDeviation si hay horas extras, null caso contrario.
   */
  public TimeDeviation calculateOvertime(AssignedShift shift, LocalDateTime punchOutTime) {
    if (punchOutTime.isAfter(shift.getExpectedEnd())) {
      long overtimeMinutes = Duration.between(shift.getExpectedEnd(), punchOutTime).toMinutes();
      if (overtimeMinutes > OVERTIME_THRESHOLD_MINUTES) {
        // Redondeo al cuarto de hora más cercano
        int roundedMinutes = (int) (Math.round(overtimeMinutes / 15.0) * 15);
        return new TimeDeviation(DeviationType.PENDING_OVERTIME, roundedMinutes, "PENDING");
      }
    }
    return null;
  }
}
