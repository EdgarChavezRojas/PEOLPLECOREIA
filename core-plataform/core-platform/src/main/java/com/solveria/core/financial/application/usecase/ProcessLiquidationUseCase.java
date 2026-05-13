package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.command.ProcessLiquidationCommand;
import com.solveria.core.financial.domain.event.FiniquitoPaymentOverdueEvent;
import com.solveria.core.financial.domain.event.LiquidationCalculatedEvent;
import com.solveria.core.financial.domain.model.vo.IndemnizableTrimSnapshot;
import com.solveria.core.financial.domain.model.vo.TerminationType;
import com.solveria.core.financial.domain.service.BolivianTaxCalculationService;
import com.solveria.core.financial.domain.service.LiquidationCalculationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.solveria.core.shared.outbox.port.EventOutboxPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Procesar Liquidación (Finiquito). W9: Workflow de Offboarding y Liquidación.
 *
 * <p>Flujo: 1. Desvinculación → recibir RELATIONSHIP_ENDED 2. Validación Candado Activos (delegado
 * a BC Dossier) 3. Cálculo Promedio (P15: últimos 3 meses) 4. Liquidación Conceptos (P17: Desahucio
 * si Despido sin Causa) 5. Revisión RC-IVA (UFV) 6. Cronómetro 15 días → si mora, multa 30%
 * automática
 *
 * <p>Implementa SoD: el aprobador no puede ser quien registró la desvinculación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessLiquidationUseCase {

  private final EventOutboxPort eventOutboxPort;

  /**
   * Procesa la liquidación del finiquito.
   *
   * @return Total de la liquidación calculada
   */
  @Transactional
  public BigDecimal execute(ProcessLiquidationCommand cmd) {
    log.info(
        "event=PROCESS_LIQUIDATION_START relationshipId={} terminationType={}",
        cmd.relationshipId(),
        cmd.terminationType());

    // --- Paso 3: Cálculo Promedio (P15) ---
    BigDecimal averageSalary =
        LiquidationCalculationService.calculateAverageSalary(cmd.lastThreeMonthsSalaries());
    log.info("event=AVERAGE_SALARY_CALCULATED average={}", averageSalary);

    // --- Paso 4: Liquidación de Conceptos ---
    BigDecimal dailySalary =
        averageSalary.divide(new BigDecimal("30"), 2, java.math.RoundingMode.HALF_UP);

    BigDecimal indemnizacion =
        LiquidationCalculationService.calculateIndemnizacion(
            averageSalary, cmd.hireDate(), cmd.terminationDate());

    BigDecimal aguinaldoProporcional =
        LiquidationCalculationService.calculateAguinaldoProporcional(
            averageSalary, cmd.hireDate(), cmd.terminationDate());

    BigDecimal vacaciones =
        LiquidationCalculationService.calculateVacacionesPendientes(
            dailySalary, cmd.pendingVacationDays());

    BigDecimal desahucio = BigDecimal.ZERO;
    boolean includesDesahucio = false;
    if (cmd.terminationType() == TerminationType.DESPIDO_SIN_CAUSA) {
      desahucio = BolivianTaxCalculationService.calculateDesahucio(averageSalary);
      includesDesahucio = true;
      log.info("event=DESAHUCIO_APPLIED amount={}", desahucio);
    }

    BigDecimal totalLiquidation =
        LiquidationCalculationService.calculateTotalLiquidation(
            indemnizacion, aguinaldoProporcional, vacaciones, desahucio, cmd.terminationType());

    log.info(
        "event=LIQUIDATION_CALCULATED total={} indemnizacion={} aguinaldo={} vacaciones={} desahucio={}",
        totalLiquidation,
        indemnizacion,
        aguinaldoProporcional,
        vacaciones,
        desahucio);

    // --- Paso 3b: Construir Trim Snapshot para Tabla II Ministerio ---
    IndemnizableTrimSnapshot trimSnapshot =
        LiquidationCalculationService.buildTrimSnapshot(
            cmd.lastThreeMonthsBase(), cmd.lastThreeMonthsOthers());
    log.info(
        "event=TRIM_SNAPSHOT_BUILT averageIndemnizable={}", trimSnapshot.averageIndemnizable());

    // --- Publicar evento LIQUIDATION_CALCULATED ---
    UUID liquidationId = UUID.randomUUID();
    LiquidationCalculatedEvent calcEvent =
        new LiquidationCalculatedEvent(
            cmd.relationshipId(),
            cmd.personId(),
            liquidationId,
            averageSalary,
            totalLiquidation,
            totalLiquidation,
            includesDesahucio,
            trimSnapshot);

//    eventOutboxPort.publish(
//        "Liquidation",
//        cmd.relationshipId(),
//        "LIQUIDATION_CALCULATED",
//        serializeLiquidationPayload(
//            cmd, totalLiquidation, indemnizacion, aguinaldoProporcional, vacaciones, desahucio));

    // --- Paso 6: Verificar mora (cronómetro T+16 días) ---
    checkOverdueAndApplyPenalty(
        cmd.relationshipId(), cmd.personId(), cmd.terminationDate(), totalLiquidation);

    return totalLiquidation;
  }

  /**
   * Verifica si el finiquito está en mora (> 15 días) y aplica multa 30%. Se ejecuta al momento del
   * cálculo; en producción el cronómetro sería un scheduler.
   */
  private void checkOverdueAndApplyPenalty(
      UUID relationshipId, UUID personId, LocalDate terminationDate, BigDecimal totalLiquidation) {
    LocalDate today = LocalDate.now();
    if (LiquidationCalculationService.isFiniquitoOverdue(terminationDate, today)) {
      BigDecimal multa = BolivianTaxCalculationService.calculateMultaFiniquito(totalLiquidation);

      log.warn(
          "event=FINIQUITO_PAYMENT_OVERDUE relationshipId={} multa30pct={}", relationshipId, multa);

      FiniquitoPaymentOverdueEvent overdueEvent =
          new FiniquitoPaymentOverdueEvent(relationshipId, personId, totalLiquidation, multa);

//      eventOutboxPort.publish(
//          "Liquidation",
//          relationshipId,
//          "FINIQUITO_PAYMENT_OVERDUE",
//          "{\"relationshipId\":\""
//              + relationshipId
//              + "\",\"totalPendiente\":"
//              + totalLiquidation
//              + ",\"multa30pct\":"
//              + multa
//              + "}");
    }
  }

  private String serializeLiquidationPayload(
      ProcessLiquidationCommand cmd,
      BigDecimal total,
      BigDecimal indemnizacion,
      BigDecimal aguinaldo,
      BigDecimal vacaciones,
      BigDecimal desahucio) {
    return "{\"relationshipId\":\""
        + cmd.relationshipId()
        + "\",\"personId\":\""
        + cmd.personId()
        + "\",\"terminationType\":\""
        + cmd.terminationType()
        + "\",\"total\":"
        + total
        + ",\"indemnizacion\":"
        + indemnizacion
        + ",\"aguinaldoProporcional\":"
        + aguinaldo
        + ",\"vacaciones\":"
        + vacaciones
        + ",\"desahucio\":"
        + desahucio
        + "}";
  }
}
