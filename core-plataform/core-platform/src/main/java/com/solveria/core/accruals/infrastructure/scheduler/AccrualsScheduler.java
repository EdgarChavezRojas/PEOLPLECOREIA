package com.solveria.core.accruals.infrastructure.scheduler;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import com.solveria.core.accruals.domain.model.vo.SeniorityMilestone;
import com.solveria.core.accruals.domain.model.vo.SenioritySpan;
import com.solveria.core.accruals.domain.policy.SeniorityBasePolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccrualsScheduler {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;
  private final BenefitsRepositoryPort benefitsRepository;
  private final RelationshipRepositoryPort relationshipRepository;

  /**
   * CRON MENSUAL DE VACACIONES Y ANTIGÜEDAD (runMonthlyVacationAndSeniority) Se ejecuta el día 1 de
   * cada mes. Evalúa el 'SenioritySpan' de cada empleado activo para inyectar los días de vacación
   * correspondientes según la escala legal boliviana (15, 20 o 30 días) y registra los hitos de
   * antigüedad (SeniorityMilestone) necesarios para activar los multiplicadores del Bono de
   * Antigüedad.
   */
  @Scheduled(cron = "0 0 0 1 * ?")
  @Transactional
  public void runMonthlyVacationAndSeniority() {
    LocalDate today = LocalDate.now();
    List<AccrualBalance> balances = accrualBalanceRepository.findAll();

    for (AccrualBalance balance : balances) {
      if (balance.getBalanceType() != AccrualBalanceType.VACATION
          || balance.getUnit() != AccrualUnit.DAYS) {
        continue;
      }

      withTenant(
          balance.getTenantId(),
          () -> {
            Optional<Relationship> relationship =
                relationshipRepository.findByRelationshipIdAndTenantId(
                    balance.getRelationshipId(), balance.getTenantId());
            if (relationship.isEmpty()) {
              log.warn(
                  "event=ACCRUALS_SCHEDULER_RELATIONSHIP_NOT_FOUND relationshipId={} tenantId={}",
                  balance.getRelationshipId(),
                  balance.getTenantId());
              return;
            }

            if (shouldAccrue(balance.getLastAccrualDate(), today)) {
              int yearsOfService =
                  Period.between(relationship.get().getHireDate(), today).getYears();
              balance.accrueVacation(yearsOfService, today);
            }

            SenioritySpan span = balance.computeSenioritySpan(relationship.get().getHireDate());
            int monthsCompleted = span.totalMonths();
            if (monthsCompleted > 0 && !hasMilestone(balance, monthsCompleted)) {
              SeniorityMilestone milestone =
                  new SeniorityMilestone(
                      UUID.randomUUID(),
                      monthsCompleted,
                      SeniorityBasePolicy.resolveBaseType(null));
              balance.addSeniorityMilestone(milestone);
            }

            accrualBalanceRepository.save(balance);
            log.info(
                "event=ACCRUALS_SCHEDULER_MONTHLY_VACATION relationshipId={} tenantId={}",
                balance.getRelationshipId(),
                balance.getTenantId());
          });
    }
  }

  /**
   * CRON MENSUAL DE PROVISIONES FINANCIERAS (runMonthlyFinancialProvisions) Se ejecuta el día 1 de
   * cada mes (desfasado 5 minutos). Inyecta la provisión contable mensual a las bóvedas de
   * Quinquenio y Beneficios (Aguinaldo, Prima). Garantiza el fondeo progresivo de los pasivos
   * laborales (típicamente sumando el 8.33% mensual).
   */
  @Scheduled(cron = "0 5 0 1 * ?")
  @Transactional
  public void runMonthlyFinancialProvisions() {
    List<QuinquenioProvision> quinquenioProvisions =
        benefitsRepository.findAllQuinquenioProvisions();
    for (QuinquenioProvision provision : quinquenioProvisions) {
      withTenant(
          provision.getTenantId(),
          () -> {
            Optional<BigDecimal> monthlyAmount = resolveMonthlyQuinquenioAmount(provision);
            if (monthlyAmount.isEmpty() || monthlyAmount.get().signum() <= 0) {
              log.warn(
                  "event=ACCRUALS_SCHEDULER_QUINQUENIO_AMOUNT_MISSING relationshipId={} tenantId={}",
                  provision.getRelationshipId(),
                  provision.getTenantId());
              return;
            }
            provision.addMonthlyProvision(monthlyAmount.get());
            benefitsRepository.saveQuinquenio(provision);
          });
    }

    List<BenefitAccrual> benefitAccruals = benefitsRepository.findAllBenefitAccruals();
    for (BenefitAccrual accrual : benefitAccruals) {
      withTenant(
          accrual.getTenantId(),
          () -> {
            Optional<BigDecimal> monthlyAmount = resolveMonthlyBenefitAmount(accrual);
            if (monthlyAmount.isEmpty() || monthlyAmount.get().signum() <= 0) {
              log.warn(
                  "event=ACCRUALS_SCHEDULER_BENEFIT_AMOUNT_MISSING relationshipId={} tenantId={} benefitType={}",
                  accrual.getRelationshipId(),
                  accrual.getTenantId(),
                  accrual.getBenefitType());
              return;
            }
            accrual.addAccrual(monthlyAmount.get());
            benefitsRepository.saveBenefitAccrual(accrual);
          });
    }
  }

  /**
   * CRON DIARIO DE AUDITORÍA LEGAL (runDailyLegalAudit) Se ejecuta diariamente a la medianoche.
   * Revisa todas las provisiones de Quinquenio que tienen un proceso de pago solicitado. Si detecta
   * que han pasado más de 30 días calendario sin registro de desembolso, activa irreversiblemente
   * la multa del 30% (P8 de la normativa).
   */
  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional
  public void runDailyLegalAudit() {
    LocalDate today = LocalDate.now();
    List<QuinquenioProvision> provisions = benefitsRepository.findAllQuinquenioProvisions();

    for (QuinquenioProvision provision : provisions) {
      withTenant(
          provision.getTenantId(),
          () -> {
            Optional<LocalDate> requestDate = resolveQuinquenioRequestDate(provision);
            Optional<LocalDate> paymentDate = resolveQuinquenioPaymentDate(provision);
            if (requestDate.isEmpty()) {
              log.warn(
                  "event=ACCRUALS_SCHEDULER_QUINQUENIO_REQUEST_DATE_MISSING relationshipId={} tenantId={}",
                  provision.getRelationshipId(),
                  provision.getTenantId());
              return;
            }
            provision.evaluatePenalty(requestDate.get(), today, paymentDate.orElse(null));
            benefitsRepository.saveQuinquenio(provision);
          });
    }
  }

  private boolean shouldAccrue(LocalDate lastAccrualDate, LocalDate today) {
    if (lastAccrualDate == null) {
      return true;
    }
    YearMonth last = YearMonth.from(lastAccrualDate);
    YearMonth current = YearMonth.from(today);
    return !last.equals(current);
  }

  private boolean hasMilestone(AccrualBalance balance, int monthsCompleted) {
    List<SeniorityMilestone> milestones = balance.getSeniorityMilestones();
    if (milestones == null || milestones.isEmpty()) {
      return false;
    }
    return milestones.stream()
        .anyMatch(milestone -> milestone.monthsCompleted() == monthsCompleted);
  }

  private void withTenant(UUID tenantId, Runnable action) {
    if (tenantId != null) {
      SecurityTenantContext.setTenantId(tenantId.toString());
    }
    try {
      action.run();
    } finally {
      SecurityTenantContext.clear();
    }
  }

  /**
   * resolveMonthlyQuinquenioAmount PROPÓSITO: Conectar con el contexto de Nómina (Payroll /
   * Financial Snapshot) para obtener la base salarial indemnizable del empleado en el mes en curso
   * y calcular el monto exacto a provisionar (el 8.33% del Total Ganado).
   */
  private Optional<BigDecimal> resolveMonthlyQuinquenioAmount(QuinquenioProvision provision) {
    // TODO: Resolver monto mensual desde nómina/financial snapshot.
    if (provision == null) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * resolveMonthlyBenefitAmount PROPÓSITO: Consultar al módulo de Nómina/Finanzas la base de
   * cálculo vigente para obtener la duodécima correspondiente al mes actual, permitiendo
   * provisionar financieramente los saldos de Aguinaldo y/o Prima de Utilidades (P16).
   */
  private Optional<BigDecimal> resolveMonthlyBenefitAmount(BenefitAccrual accrual) {
    // TODO: Resolver monto mensual desde nómina/financial snapshot.
    if (accrual == null) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * resolveMonthlyBenefitAmount PROPÓSITO: Consultar al módulo de Nómina/Finanzas la base de
   * cálculo vigente para obtener la duodécima correspondiente al mes actual, permitiendo
   * provisionar financieramente los saldos de Aguinaldo y/o Prima de Utilidades (P16).
   */
  private Optional<LocalDate> resolveQuinquenioRequestDate(QuinquenioProvision provision) {
    // TODO: Obtener fecha de solicitud desde el registro de pagos/solicitudes.
    if (provision == null) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * resolveQuinquenioPaymentDate PROPÓSITO: Consultar al módulo de Tesorería o Integración Bancaria
   * si ya existe un comprobante de pago efectivo. Retornar esta fecha le indica al Scheduler que el
   * beneficio ha sido liquidado y debe apagar el cronómetro de penalizaciones.
   */
  private Optional<LocalDate> resolveQuinquenioPaymentDate(QuinquenioProvision provision) {
    // TODO: Obtener fecha de pago desde tesorería o ERP.
    if (provision == null) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
