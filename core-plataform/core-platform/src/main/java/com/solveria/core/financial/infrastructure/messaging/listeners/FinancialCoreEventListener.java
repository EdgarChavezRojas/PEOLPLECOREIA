package com.solveria.core.financial.infrastructure.messaging.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.experience.domain.event.DataChangeRequestedEvent;
import com.solveria.core.financial.application.command.ImputeAnalyticCommand;
import com.solveria.core.financial.application.command.ProcessLiquidationCommand;
import com.solveria.core.financial.application.port.ContractFinancialDataPort;
import com.solveria.core.financial.application.port.OrgUnitFinancialDataPort;
import com.solveria.core.financial.application.port.QuinquenioSalaryDataPort;
import com.solveria.core.financial.application.usecase.ImputeAnalyticTerritorialUseCase;
import com.solveria.core.financial.application.usecase.ProcessLiquidationUseCase;
import com.solveria.core.financial.application.usecase.ProcessQuinquenioPaymentUseCase;
import com.solveria.core.financial.application.usecase.SyncBankAccountUseCase;
import com.solveria.core.financial.application.usecase.ValidateFundingSourceUseCase;
import com.solveria.core.financial.domain.event.QuinquenioRequestedEvent;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractDraftedEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitAssignedChangedEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener de eventos cross-BC para el Bounded Context Financial.
 *
 * <p>Responsabilidad única: recibir eventos de dominio de otros BCs (Workforce, Legal, Accruals,
 * Experience) y delegar la ejecución al Caso de Uso correspondiente. PROHIBIDO colocar lógica de
 * negocio, cálculos, hardcode o fabricación de datos en esta clase.
 *
 * <p>Los datos que el evento no transporta se resuelven a través de puertos ACL (Anti-Corruption
 * Layer) inyectados, nunca se fabrican ni se hardcodean.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinancialCoreEventListener {

  // ── Use Cases ────────────────────────────────────────────────────────────
  private final ImputeAnalyticTerritorialUseCase imputeAnalyticTerritorialUseCase;
  private final ValidateFundingSourceUseCase validateFundingSourceUseCase;
  private final ProcessLiquidationUseCase processLiquidationUseCase;
  private final ProcessQuinquenioPaymentUseCase processQuinquenioPaymentUseCase;
  private final SyncBankAccountUseCase syncBankAccountUseCase;

  // ── ACL Ports (resolución de datos cross-BC) ─────────────────────────────
  private final ContractFinancialDataPort contractFinancialDataPort;
  private final OrgUnitFinancialDataPort orgUnitFinancialDataPort;
  private final QuinquenioSalaryDataPort quinquenioSalaryDataPort;

  // ── Infraestructura ──────────────────────────────────────────────────────
  private final ObjectMapper objectMapper;

  // ──────────────────────────────────────────────────────────────────────────
  // 1. OrgUnitAssignedChangedEvent → ImputeAnalyticTerritorialUseCase
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona al cambio de asignación de unidad organizativa (Workforce BC). Delega la construcción
   * del comando al {@link OrgUnitFinancialDataPort} y la ejecución al caso de uso.
   */
  @EventListener
  @Transactional
  public void handle(OrgUnitAssignedChangedEvent event) {
    log.info(
        "event=FINANCIAL_ORGUNIT_CHANGE_RECEIVED unitId={} newParentId={}",
        event.unitId(),
        event.newParentId());
    try {
      ImputeAnalyticCommand command =
          orgUnitFinancialDataPort.buildImputeAnalyticCommand(event.unitId(), event.newParentId());

      imputeAnalyticTerritorialUseCase.execute(command);

      log.info("event=FINANCIAL_ORGUNIT_CHANGE_PROCESSED unitId={}", event.unitId());
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_ORGUNIT_CHANGE_FAILED unitId={} error={}",
          event.unitId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 2. ContractDraftedEvent → ValidateFundingSourceUseCase.validateFundingSource
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona al borrador de contrato (Legal BC). Realiza la reserva preventiva (Pre-encumbrance)
   * validando la suficiencia de fondos. Los datos presupuestarios se resuelven a través del {@link
   * ContractFinancialDataPort}.
   */
  @EventListener
  @Transactional
  public void handle(ContractDraftedEvent event) {
    log.info(
        "event=FINANCIAL_CONTRACT_DRAFTED_RECEIVED contractId={} relationshipId={}",
        event.contractId(),
        event.relationshipId());
    try {
      UUID sourceId = contractFinancialDataPort.getFundingSourceIdForContract(event.contractId());
      BigDecimal requiredAmount =
          contractFinancialDataPort.getRequiredBudgetForContract(event.contractId());
      String approver = contractFinancialDataPort.getApproverForContract(event.contractId());

      validateFundingSourceUseCase.validateFundingSource(sourceId, requiredAmount, approver);

      log.info(
          "event=FINANCIAL_CONTRACT_DRAFTED_PROCESSED contractId={} sourceId={}",
          event.contractId(),
          sourceId);
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_CONTRACT_DRAFTED_FAILED contractId={} error={}",
          event.contractId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 3. ContractApprovedEvent → ValidateFundingSourceUseCase.allocateBudget
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a la aprobación de contrato (Legal BC). Formaliza la asignación presupuestaria
   * contable. El monto y la fuente se resuelven a través del {@link ContractFinancialDataPort}.
   */
  @EventListener
  @Transactional
  public void handle(ContractApprovedEvent event) {
    log.info(
        "event=FINANCIAL_CONTRACT_APPROVED_RECEIVED contractId={} tenantId={}",
        event.contractId(),
        event.tenantId());
    try {
      UUID sourceId = contractFinancialDataPort.getFundingSourceIdForContract(event.contractId());
      BigDecimal amount =
          contractFinancialDataPort.getAllocationAmountForContract(event.contractId());

      validateFundingSourceUseCase.allocateBudget(sourceId, amount);

      log.info(
          "event=FINANCIAL_CONTRACT_APPROVED_PROCESSED contractId={} sourceId={} amount={}",
          event.contractId(),
          sourceId,
          amount);
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_CONTRACT_APPROVED_FAILED contractId={} error={}",
          event.contractId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 4. ContractTerminatedEvent → ProcessLiquidationUseCase
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a la terminación de contrato (Legal BC). El {@link ContractFinancialDataPort}
   * construye el comando completo con datos reales (salarios, tipo de terminación, etc.).
   */
  @EventListener
  @Transactional
  public void handle(ContractTerminatedEvent event) {
    log.info("event=FINANCIAL_CONTRACT_TERMINATED_RECEIVED contractId={}", event.contractId());
    try {
      ProcessLiquidationCommand command =
          contractFinancialDataPort.buildLiquidationCommand(event.contractId());

      processLiquidationUseCase.execute(command);

      log.info("event=FINANCIAL_CONTRACT_TERMINATED_PROCESSED contractId={}", event.contractId());
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_CONTRACT_TERMINATED_FAILED contractId={} error={}",
          event.contractId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 5. QuinquenioRequestedEvent → ProcessQuinquenioPaymentUseCase
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a la solicitud de quinquenio (Accruals/Financial BC). Los datos salariales detallados
   * y los meses de antigüedad se resuelven a través del {@link QuinquenioSalaryDataPort}.
   */
  @EventListener
  @Transactional
  public void handle(QuinquenioRequestedEvent event) {
    log.info(
        "event=FINANCIAL_QUINQUENIO_REQUESTED_RECEIVED personId={} amount={} deadline={}",
        event.personId(),
        event.quinquenioAmount(),
        event.paymentDeadline());
    try {
      int continuousMonths = quinquenioSalaryDataPort.getContinuousMonths(event.personId());
      List<BigDecimal> salaryBase =
          quinquenioSalaryDataPort.getLastThreeMonthsBase(event.personId());
      List<BigDecimal> salaryOthers =
          quinquenioSalaryDataPort.getLastThreeMonthsOthers(event.personId());

      processQuinquenioPaymentUseCase.execute(
          event.personId(), continuousMonths, salaryBase, salaryOthers);

      log.info("event=FINANCIAL_QUINQUENIO_REQUESTED_PROCESSED personId={}", event.personId());
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_QUINQUENIO_REQUESTED_FAILED personId={} error={}",
          event.personId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 6. DataChangeRequestedEvent → SyncBankAccountUseCase
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a solicitudes de cambio de datos (Experience BC). Evalúa si el {@code actionType}
   * corresponde a datos bancarios. Si lo es, parsea el payload JSON y delega al caso de uso de
   * sincronización de cuenta bancaria.
   */
  @EventListener
  @Transactional
  public void handle(DataChangeRequestedEvent event) {
    log.info(
        event.getClass().getSimpleName(), event.actionId(), event.personId(), event.actionType());

    if (!isBankAccountAction(event.actionType())) {
      log.info(
          "event=FINANCIAL_DATA_CHANGE_SKIPPED actionId={} actionType={} reason=NOT_BANK_ACTION",
          event.actionId(),
          event.actionType());
      return;
    }

    try {
      JsonNode payloadNode = objectMapper.readTree(event.payload());

      String bankAccountNumber = extractRequiredField(payloadNode, "bankAccountNumber");
      String bankCode = extractRequiredField(payloadNode, "bankCode");

      if (bankAccountNumber == null || bankCode == null) {
        log.warn(
            "event=FINANCIAL_DATA_CHANGE_INVALID_PAYLOAD actionId={} reason=MISSING_BANK_FIELDS",
            event.actionId());
        return;
      }

      syncBankAccountUseCase.syncBankAccount(
          event.personId(),
          bankAccountNumber,
          bankCode,
          event.tenantId(),
          event.actionId().toString());

      log.info(
          "event=FINANCIAL_DATA_CHANGE_PROCESSED actionId={} personId={}",
          event.actionId(),
          event.personId());
    } catch (Exception ex) {
      log.warn(
          "event=FINANCIAL_DATA_CHANGE_FAILED actionId={} error={}",
          event.actionId(),
          ex.getMessage(),
          ex);
    }
  }

  // ── Métodos auxiliares de infraestructura (parsing) ───────────────────────

  /**
   * Determina si el tipo de acción corresponde a una actualización bancaria. Utiliza
   * case-insensitive para robustez ante variantes de casing del BC emisor.
   */
  private boolean isBankAccountAction(String actionType) {
    return actionType != null && actionType.toUpperCase().contains("BANK_ACCOUNT");
  }

  /**
   * Extrae un campo requerido del nodo JSON. Retorna {@code null} si el campo no existe o está
   * vacío, delegando la validación al llamador.
   */
  private String extractRequiredField(JsonNode node, String fieldName) {
    String value = node.path(fieldName).asText("");
    return value.isBlank() ? null : value;
  }
}
