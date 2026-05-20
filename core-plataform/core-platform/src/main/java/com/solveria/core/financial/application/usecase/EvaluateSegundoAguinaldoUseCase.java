package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.domain.event.SegundoAguinaldoEligibilityMetEvent;
import com.solveria.core.financial.domain.service.SegundoAguinaldoPolicy;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Evaluar Elegibilidad de Segundo Aguinaldo. Trigger: GdpGrowthExceedsThreshold
 * (resolución gubernamental anual).
 *
 * <p>Delega al SegundoAguinaldoPolicy para determinar si el crecimiento del PIB supera el umbral
 * legal. Si aplica, calcula el monto provisional y emite SEGUNDO_AGUINALDO_ELIGIBILITY_MET vía
 * Outbox.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluateSegundoAguinaldoUseCase extends DomainRoot {

  private final EventOutboxPort eventOutboxPort;

  /**
   * Evalúa la elegibilidad del Segundo Aguinaldo para un empleado específico.
   *
   * @param personId ID del empleado
   * @param averageSalary Salario promedio (P15: últimos 3 meses Total Ganado)
   * @param gdpGrowthToggleActive Flag externo: true si el gobierno decretó el pago
   * @return Monto provisional calculado (ZERO si no aplica)
   */
  @Transactional
  public BigDecimal execute(
      UUID personId, BigDecimal averageSalary, boolean gdpGrowthToggleActive) {
    log.info(
        "event=EVALUATE_SEGUNDO_AGUINALDO personId={} gdpToggle={}",
        personId,
        gdpGrowthToggleActive);

    if (!SegundoAguinaldoPolicy.isEligible(gdpGrowthToggleActive)) {
      log.info("event=SEGUNDO_AGUINALDO_NOT_ELIGIBLE personId={}", personId);
      return BigDecimal.ZERO;
    }

    BigDecimal provisionalAmount = SegundoAguinaldoPolicy.calculate(averageSalary, true);

    if (provisionalAmount.compareTo(BigDecimal.ZERO) > 0) {

      registerEvent(new SegundoAguinaldoEligibilityMetEvent(personId, provisionalAmount));
      // revisar si es valido para la arquitectura y el acoplamiento si se puede extender un caso de
      // uso a un dominio

      log.info(
          "event=SEGUNDO_AGUINALDO_ELIGIBILITY_MET personId={} provisionalAmount={}",
          personId,
          provisionalAmount);
    }

    return provisionalAmount;
  }
}
