package com.solveria.core.financial.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Cálculo de Aguinaldo Navideño masivo por Tenant.
 *
 * <p>Flujo:
 * 1. Recibe tenantId y año fiscal.
 * 2. Filtra elegibles: empleados >= 1 mes trabajado, obreros >= 3 meses.
 * 3. Base de cálculo (P15): Promedio Total Ganado de Septiembre, Octubre, Noviembre.
 * 4. Cálculo: Proporcional en duodécimas si meses trabajados < 12, caso contrario 1 sueldo promedio.
 * 5. Invariante Multa (P16): Si LocalDate.now() > 20 Diciembre, multiplicar el pago final por 2.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateAnnualAguinaldoUseCase {

  /** Meses mínimos para elegibilidad de empleados. */
  private static final int MIN_MONTHS_EMPLEADO = 1;

  /** Meses mínimos para elegibilidad de obreros. */
  private static final int MIN_MONTHS_OBRERO = 3;

  /** Total de duodécimas en un año. */
  private static final BigDecimal TWELVE = new BigDecimal("12");

  /** Factor de multa por pago tardío (P16: después del 20 de Diciembre). */
  private static final BigDecimal PENALTY_MULTIPLIER = new BigDecimal("2");

  /** Día límite de diciembre para pago sin penalización. */
  private static final int DECEMBER_DEADLINE_DAY = 20;

  /**
   * Calcula el Aguinaldo Navideño para un empleado individual.
   *
   * @param personId Identificador del empleado (para trazabilidad)
   * @param tenantId Identificador del Tenant
   * @param fiscalYear Año fiscal de cálculo
   * @param monthsWorked Meses trabajados en el año fiscal
   * @param isObrero true si el trabajador es obrero, false si es empleado
   * @param septemberSalary Total Ganado de Septiembre
   * @param octoberSalary Total Ganado de Octubre
   * @param novemberSalary Total Ganado de Noviembre
   * @return Monto del Aguinaldo (ZERO si no es elegible)
   */
  @Transactional(readOnly = true)
  public BigDecimal execute(
      java.util.UUID personId,
      String tenantId,
      int fiscalYear,
      int monthsWorked,
      boolean isObrero,
      BigDecimal septemberSalary,
      BigDecimal octoberSalary,
      BigDecimal novemberSalary) {

    log.info(
        "event=AGUINALDO_CALCULATION_START personId={} tenantId={} fiscalYear={} monthsWorked={} isObrero={}",
        personId,
        tenantId,
        fiscalYear,
        monthsWorked,
        isObrero);

    // --- Paso 2: Validar elegibilidad ---
    int minMonths = isObrero ? MIN_MONTHS_OBRERO : MIN_MONTHS_EMPLEADO;
    if (monthsWorked < minMonths) {
      log.info(
          "event=AGUINALDO_NOT_ELIGIBLE personId={} monthsWorked={} minRequired={}",
          personId,
          monthsWorked,
          minMonths);
      return BigDecimal.ZERO;
    }

    // --- Paso 3: Base de cálculo (P15): Promedio Sep/Oct/Nov ---
    BigDecimal totalTrimestre =
        safe(septemberSalary).add(safe(octoberSalary)).add(safe(novemberSalary));

    long activeSalaryMonths =
        List.of(septemberSalary, octoberSalary, novemberSalary).stream()
            .filter(s -> s != null && s.compareTo(BigDecimal.ZERO) > 0)
            .count();

    BigDecimal averageSalary;
    if (activeSalaryMonths == 0) {
      log.warn("event=AGUINALDO_NO_SALARY_DATA personId={}", personId);
      return BigDecimal.ZERO;
    }
    averageSalary =
        totalTrimestre.divide(new BigDecimal(activeSalaryMonths), 2, RoundingMode.HALF_UP);

    log.info(
        "event=AGUINALDO_AVERAGE_CALCULATED personId={} average={} activeSalaryMonths={}",
        personId,
        averageSalary,
        activeSalaryMonths);

    // --- Paso 4: Cálculo proporcional o completo ---
    BigDecimal aguinaldo;
    if (monthsWorked >= 12) {
      aguinaldo = averageSalary.setScale(2, RoundingMode.HALF_UP);
    } else {
      aguinaldo =
          averageSalary
              .multiply(new BigDecimal(monthsWorked))
              .divide(TWELVE, 2, RoundingMode.HALF_UP);
    }

    log.info(
        "event=AGUINALDO_BASE_CALCULATED personId={} baseAmount={} monthsWorked={}",
        personId,
        aguinaldo,
        monthsWorked);

    // --- Paso 5: Invariante P16 — Multa si pago > 20 Diciembre ---
    LocalDate today = LocalDate.now();
    boolean penaltyApplied = false;
    if (today.getMonth() == Month.DECEMBER && today.getDayOfMonth() > DECEMBER_DEADLINE_DAY) {
      aguinaldo = aguinaldo.multiply(PENALTY_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
      penaltyApplied = true;
      log.warn(
          "event=AGUINALDO_PENALTY_APPLIED personId={} penalizedAmount={} today={}",
          personId,
          aguinaldo,
          today);
    }

    log.info(
        "event=AGUINALDO_CALCULATED personId={} finalAmount={} penaltyApplied={}",
        personId,
        aguinaldo,
        penaltyApplied);

    return aguinaldo;
  }

  private static BigDecimal safe(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
