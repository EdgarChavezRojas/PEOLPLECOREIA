package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.domain.event.QuinquenioRequestedEvent;
import com.solveria.core.financial.domain.model.vo.IndemnizableTrimSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Procesar Pago de Quinquenio (standalone, sin desvinculación).
 *
 * <p>Flujo:
 * 1. Recibe personId y datos salariales de los últimos 3 meses.
 * 2. Valida elegibilidad: el empleado debe tener >= 60 meses continuos de antigüedad.
 * 3. Calcula base: Promedio Total Ganado de últimos 3 meses (P15, via IndemnizableTrimSnapshot).
 * 4. Cálculo total: Promedio × 5.
 * 5. Invariante: NO aplica deducciones de ley (exento de RC-IVA y Gestora).
 * 6. Dispara evento QuinquenioRequestedEvent con cronómetro de 30 días para pago.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessQuinquenioPaymentUseCase extends DomainRoot {

  /** Meses continuos mínimos requeridos para elegibilidad de quinquenio. */
  private static final int REQUIRED_CONTINUOUS_MONTHS = 60;

  /** Multiplicador legal del quinquenio (5 salarios promedio). */
  private static final BigDecimal QUINQUENIO_MULTIPLIER = new BigDecimal("5");

  /** Días calendario para plazo de pago del quinquenio. */
  private static final int PAYMENT_DEADLINE_DAYS = 30;

  /**
   * Procesa el cálculo del quinquenio para un empleado elegible.
   *
   * @param personId ID del empleado
   * @param continuousMonths Meses continuos de antigüedad del empleado
   * @param lastThreeMonthsBase Sueldo base de los últimos 3 meses (cronológico)
   * @param lastThreeMonthsOthers Otros conceptos de los últimos 3 meses (cronológico)
   * @return Monto total del quinquenio (ZERO si no elegible)
   */
  @Transactional
  public BigDecimal execute(
      UUID personId,
      int continuousMonths,
      List<BigDecimal> lastThreeMonthsBase,
      List<BigDecimal> lastThreeMonthsOthers) {

    log.info(
        "event=PROCESS_QUINQUENIO_START personId={} continuousMonths={}",
        personId,
        continuousMonths);

    // --- Paso 2: Validar elegibilidad (60 meses continuos) ---
    if (continuousMonths < REQUIRED_CONTINUOUS_MONTHS) {
      log.info(
          "event=QUINQUENIO_NOT_ELIGIBLE personId={} continuousMonths={} required={}",
          personId,
          continuousMonths,
          REQUIRED_CONTINUOUS_MONTHS);
      return BigDecimal.ZERO;
    }

    // --- Paso 3: Cálculo Promedio (P15) via IndemnizableTrimSnapshot ---
    IndemnizableTrimSnapshot trimSnapshot =
        IndemnizableTrimSnapshot.build(lastThreeMonthsBase, lastThreeMonthsOthers);

    BigDecimal averageSalary = trimSnapshot.averageIndemnizable();
    log.info("event=QUINQUENIO_AVERAGE_CALCULATED personId={} average={}", personId, averageSalary);

    if (averageSalary.compareTo(BigDecimal.ZERO) <= 0) {
      log.warn("event=QUINQUENIO_INVALID_AVERAGE personId={} average={}", personId, averageSalary);
      return BigDecimal.ZERO;
    }

    // --- Paso 4: Cálculo total = Promedio × 5 ---
    BigDecimal quinquenioAmount =
        averageSalary
            .multiply(QUINQUENIO_MULTIPLIER)
            .setScale(2, RoundingMode.HALF_UP);

    log.info(
        "event=QUINQUENIO_CALCULATED personId={} amount={} multiplier={}",
        personId,
        quinquenioAmount,
        QUINQUENIO_MULTIPLIER);

    // --- Invariante: Exento de RC-IVA y Gestora (no se aplican deducciones) ---

    // --- Paso 6: Disparar evento con cronómetro de 30 días ---
    LocalDate paymentDeadline = LocalDate.now().plusDays(PAYMENT_DEADLINE_DAYS);

    registerEvent(
        new QuinquenioRequestedEvent(personId, quinquenioAmount, averageSalary, paymentDeadline));

    log.info(
        "event=QUINQUENIO_REQUESTED personId={} amount={} deadline={}",
        personId,
        quinquenioAmount,
        paymentDeadline);

    return quinquenioAmount;
  }
}
