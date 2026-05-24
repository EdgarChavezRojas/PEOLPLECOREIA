package com.solveria.core.financial.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Puerto secundario (outbound): Resuelve datos salariales históricos para el cálculo de quinquenio.
 *
 * <p>Cuando el evento {@code QuinquenioRequestedEvent} solo transporta un promedio pre-calculado,
 * este puerto permite obtener los datos salariales detallados (base + otros) de los últimos 3 meses
 * necesarios para satisfacer la firma del Use Case.
 */
public interface QuinquenioSalaryDataPort {

  /**
   * Obtiene los sueldos base de los últimos 3 meses del empleado (orden cronológico).
   *
   * @param personId ID del empleado
   * @return Lista de 3 montos base (uno por mes)
   */
  List<BigDecimal> getLastThreeMonthsBase(UUID personId);

  /**
   * Obtiene otros conceptos salariales de los últimos 3 meses del empleado (orden cronológico).
   *
   * @param personId ID del empleado
   * @return Lista de 3 montos de otros conceptos (uno por mes)
   */
  List<BigDecimal> getLastThreeMonthsOthers(UUID personId);

  /**
   * Obtiene los meses continuos de antigüedad del empleado.
   *
   * @param personId ID del empleado
   * @return Cantidad de meses continuos de antigüedad
   */
  int getContinuousMonths(UUID personId);
}
