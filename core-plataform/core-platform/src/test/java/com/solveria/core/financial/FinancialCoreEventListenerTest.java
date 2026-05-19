package com.solveria.core.financial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.solveria.core.financial.domain.model.vo.TerminationType;
import com.solveria.core.financial.infrastructure.messaging.listeners.FinancialCoreEventListener;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractDraftedEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitAssignedChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test de integración para {@link FinancialCoreEventListener}.
 *
 * <p>Valida que cada evento cross-BC publicado mediante {@link ApplicationEventPublisher} sea
 * recibido correctamente por el listener y delegado al Caso de Uso correspondiente, utilizando
 * los puertos ACL para resolver datos cross-BC. También verifica que las excepciones sean
 * capturadas y logueadas sin romper el hilo.
 */
@SpringBootTest(
    classes = {
      FinancialCoreEventListener.class,
      FinancialCoreEventListenerTest.TestConfig.class
    })
class FinancialCoreEventListenerTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Autowired private ApplicationEventPublisher eventPublisher;

  // ── Use Cases (mocks) ────────────────────────────────────────────────────
  @MockitoBean private ImputeAnalyticTerritorialUseCase imputeAnalyticTerritorialUseCase;
  @MockitoBean private ValidateFundingSourceUseCase validateFundingSourceUseCase;
  @MockitoBean private ProcessLiquidationUseCase processLiquidationUseCase;
  @MockitoBean private ProcessQuinquenioPaymentUseCase processQuinquenioPaymentUseCase;
  @MockitoBean private SyncBankAccountUseCase syncBankAccountUseCase;

  // ── ACL Ports (mocks) ────────────────────────────────────────────────────
  @MockitoBean private ContractFinancialDataPort contractFinancialDataPort;
  @MockitoBean private OrgUnitFinancialDataPort orgUnitFinancialDataPort;
  @MockitoBean private QuinquenioSalaryDataPort quinquenioSalaryDataPort;

  // ──────────────────────────────────────────────────────────────────────────
  // 1. OrgUnitAssignedChangedEvent
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("OrgUnitAssignedChangedEvent → ImputeAnalyticTerritorialUseCase")
  class OrgUnitAssignedChangedTests {

    @Test
    @DisplayName("Debe resolver datos vía OrgUnitFinancialDataPort y delegar al UC")
    void shouldDelegateToImputeAnalyticUseCase() {
      // Arrange
      UUID unitId = UUID.randomUUID();
      UUID newParentId = UUID.randomUUID();
      UUID sourceId = UUID.randomUUID();
      UUID personId = UUID.randomUUID();
      LocalDate transferDate = LocalDate.now();
      LocalDate periodStart = transferDate.withDayOfMonth(1);
      LocalDate periodEnd = transferDate.withDayOfMonth(transferDate.lengthOfMonth());

      ImputeAnalyticCommand expectedCommand =
          new ImputeAnalyticCommand(
              sourceId, personId, unitId, newParentId, transferDate, periodStart, periodEnd);

      when(orgUnitFinancialDataPort.buildImputeAnalyticCommand(unitId, newParentId))
          .thenReturn(expectedCommand);

      OrgUnitAssignedChangedEvent event =
          new OrgUnitAssignedChangedEvent(unitId, newParentId, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(orgUnitFinancialDataPort, times(1))
          .buildImputeAnalyticCommand(unitId, newParentId);
      verify(imputeAnalyticTerritorialUseCase, times(1)).execute(expectedCommand);
    }

    @Test
    @DisplayName("Debe capturar y loguear la excepción si el port o UC falla")
    void shouldCatchExceptionWhenUseCaseFails() {
      // Arrange
      UUID unitId = UUID.randomUUID();
      UUID newParentId = UUID.randomUUID();

      when(orgUnitFinancialDataPort.buildImputeAnalyticCommand(unitId, newParentId))
          .thenThrow(new RuntimeException("Datos de unidad organizativa no encontrados"));

      OrgUnitAssignedChangedEvent event =
          new OrgUnitAssignedChangedEvent(unitId, newParentId, Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(imputeAnalyticTerritorialUseCase, never()).execute(any(ImputeAnalyticCommand.class));
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 2. ContractDraftedEvent
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("ContractDraftedEvent → ValidateFundingSourceUseCase.validateFundingSource")
  class ContractDraftedTests {

    @Test
    @DisplayName("Debe resolver datos presupuestarios vía port y delegar la pre-encumbrance")
    void shouldDelegatePreEncumbranceValidation() {
      // Arrange
      UUID contractId = UUID.randomUUID();
      UUID relationshipId = UUID.randomUUID();
      UUID sourceId = UUID.randomUUID();
      BigDecimal requiredAmount = new BigDecimal("15000.00");
      String approver = "user-approver-001";

      when(contractFinancialDataPort.getFundingSourceIdForContract(contractId))
          .thenReturn(sourceId);
      when(contractFinancialDataPort.getRequiredBudgetForContract(contractId))
          .thenReturn(requiredAmount);
      when(contractFinancialDataPort.getApproverForContract(contractId))
          .thenReturn(approver);

      ContractDraftedEvent event =
          new ContractDraftedEvent(contractId, relationshipId, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(contractFinancialDataPort, times(1)).getFundingSourceIdForContract(contractId);
      verify(contractFinancialDataPort, times(1)).getRequiredBudgetForContract(contractId);
      verify(contractFinancialDataPort, times(1)).getApproverForContract(contractId);
      verify(validateFundingSourceUseCase, times(1))
          .validateFundingSource(sourceId, requiredAmount, approver);
    }

    @Test
    @DisplayName("Debe capturar la excepción si el port falla sin propagar al publisher")
    void shouldCatchExceptionWhenPortFails() {
      // Arrange
      UUID contractId = UUID.randomUUID();
      UUID relationshipId = UUID.randomUUID();

      when(contractFinancialDataPort.getFundingSourceIdForContract(contractId))
          .thenThrow(new IllegalArgumentException("Contrato sin fuente de financiamiento"));

      ContractDraftedEvent event =
          new ContractDraftedEvent(contractId, relationshipId, Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(validateFundingSourceUseCase, never())
          .validateFundingSource(any(), any(), anyString());
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 3. ContractApprovedEvent
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("ContractApprovedEvent → ValidateFundingSourceUseCase.allocateBudget")
  class ContractApprovedTests {

    @Test
    @DisplayName("Debe resolver monto y sourceId vía port y formalizar la asignación")
    void shouldDelegateBudgetAllocation() {
      // Arrange
      UUID contractId = UUID.randomUUID();
      UUID tenantId = UUID.randomUUID();
      UUID sourceId = UUID.randomUUID();
      BigDecimal allocationAmount = new BigDecimal("25000.00");

      when(contractFinancialDataPort.getFundingSourceIdForContract(contractId))
          .thenReturn(sourceId);
      when(contractFinancialDataPort.getAllocationAmountForContract(contractId))
          .thenReturn(allocationAmount);

      ContractApprovedEvent event =
          new ContractApprovedEvent(contractId, tenantId, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(contractFinancialDataPort, times(1)).getFundingSourceIdForContract(contractId);
      verify(contractFinancialDataPort, times(1)).getAllocationAmountForContract(contractId);
      verify(validateFundingSourceUseCase, times(1))
          .allocateBudget(sourceId, allocationAmount);
    }

    @Test
    @DisplayName("Debe capturar la excepción si el port falla sin romper el publisher")
    void shouldCatchExceptionWhenAllocationFails() {
      // Arrange
      UUID contractId = UUID.randomUUID();
      UUID tenantId = UUID.randomUUID();
      UUID sourceId = UUID.randomUUID();

      when(contractFinancialDataPort.getFundingSourceIdForContract(contractId))
          .thenReturn(sourceId);
      when(contractFinancialDataPort.getAllocationAmountForContract(contractId))
          .thenReturn(new BigDecimal("10000.00"));
      doThrow(new IllegalArgumentException("FundingSource no encontrado"))
          .when(validateFundingSourceUseCase)
          .allocateBudget(any(UUID.class), any(BigDecimal.class));

      ContractApprovedEvent event =
          new ContractApprovedEvent(contractId, tenantId, Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(validateFundingSourceUseCase, times(1))
          .allocateBudget(any(UUID.class), any(BigDecimal.class));
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 4. ContractTerminatedEvent
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("ContractTerminatedEvent → ProcessLiquidationUseCase")
  class ContractTerminatedTests {

    @Test
    @DisplayName("Debe delegar la construcción del comando al port y ejecutar el UC")
    void shouldDelegateLiquidationProcessing() {
      // Arrange
      UUID contractId = UUID.randomUUID();
      UUID personId = UUID.randomUUID();

      ProcessLiquidationCommand expectedCommand =
          new ProcessLiquidationCommand(
              contractId,
              personId,
              TerminationType.DESPIDO_SIN_CAUSA,
              LocalDate.of(2026, 5, 16),
              LocalDate.of(2024, 1, 15),
              List.of(
                  new BigDecimal("8000.00"),
                  new BigDecimal("8200.00"),
                  new BigDecimal("8100.00")),
              List.of(
                  new BigDecimal("7500.00"),
                  new BigDecimal("7700.00"),
                  new BigDecimal("7600.00")),
              List.of(
                  new BigDecimal("500.00"),
                  new BigDecimal("500.00"),
                  new BigDecimal("500.00")),
              12,
                  UUID.randomUUID(),
              "approver-user-002");

      when(contractFinancialDataPort.buildLiquidationCommand(contractId))
          .thenReturn(expectedCommand);
      when(processLiquidationUseCase.execute(expectedCommand))
          .thenReturn(new BigDecimal("45000.00"));

      ContractTerminatedEvent event =
          new ContractTerminatedEvent(contractId, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(contractFinancialDataPort, times(1)).buildLiquidationCommand(contractId);
      verify(processLiquidationUseCase, times(1)).execute(expectedCommand);
    }

    @Test
    @DisplayName("Debe capturar la excepción si la construcción del comando falla")
    void shouldCatchExceptionWhenBuildCommandFails() {
      // Arrange
      UUID contractId = UUID.randomUUID();

      when(contractFinancialDataPort.buildLiquidationCommand(contractId))
          .thenThrow(new RuntimeException("Contrato no encontrado para liquidación"));

      ContractTerminatedEvent event =
          new ContractTerminatedEvent(contractId, Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(processLiquidationUseCase, never()).execute(any(ProcessLiquidationCommand.class));
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 5. QuinquenioRequestedEvent
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("QuinquenioRequestedEvent → ProcessQuinquenioPaymentUseCase")
  class QuinquenioRequestedTests {

    @Test
    @DisplayName("Debe resolver datos salariales vía port y delegar al UC de quinquenio")
    void shouldDelegateQuinquenioPayment() {
      // Arrange
      UUID personId = UUID.randomUUID();
      BigDecimal quinquenioAmount = new BigDecimal("50000.00");
      BigDecimal averageSalary = new BigDecimal("10000.00");
      LocalDate paymentDeadline = LocalDate.now().plusDays(30);

      List<BigDecimal> salaryBase =
          List.of(
              new BigDecimal("9800.00"),
              new BigDecimal("10000.00"),
              new BigDecimal("10200.00"));
      List<BigDecimal> salaryOthers =
          List.of(
              new BigDecimal("200.00"),
              new BigDecimal("300.00"),
              new BigDecimal("250.00"));
      int continuousMonths = 62;

      when(quinquenioSalaryDataPort.getContinuousMonths(personId)).thenReturn(continuousMonths);
      when(quinquenioSalaryDataPort.getLastThreeMonthsBase(personId)).thenReturn(salaryBase);
      when(quinquenioSalaryDataPort.getLastThreeMonthsOthers(personId)).thenReturn(salaryOthers);
      when(processQuinquenioPaymentUseCase.execute(
              any(UUID.class), anyInt(), anyList(), anyList()))
          .thenReturn(quinquenioAmount);

      QuinquenioRequestedEvent event =
          new QuinquenioRequestedEvent(
              personId, quinquenioAmount, averageSalary, paymentDeadline, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(quinquenioSalaryDataPort, times(1)).getContinuousMonths(personId);
      verify(quinquenioSalaryDataPort, times(1)).getLastThreeMonthsBase(personId);
      verify(quinquenioSalaryDataPort, times(1)).getLastThreeMonthsOthers(personId);
      verify(processQuinquenioPaymentUseCase, times(1))
          .execute(personId, continuousMonths, salaryBase, salaryOthers);
    }

    @Test
    @DisplayName("Debe capturar la excepción si el port de salarios falla")
    void shouldCatchExceptionWhenSalaryPortFails() {
      // Arrange
      UUID personId = UUID.randomUUID();

      when(quinquenioSalaryDataPort.getContinuousMonths(personId))
          .thenThrow(new RuntimeException("Datos salariales no disponibles"));

      QuinquenioRequestedEvent event =
          new QuinquenioRequestedEvent(
              personId,
              new BigDecimal("50000.00"),
              new BigDecimal("10000.00"),
              LocalDate.now().plusDays(30),
              Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(processQuinquenioPaymentUseCase, never())
          .execute(any(UUID.class), anyInt(), anyList(), anyList());
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 6. DataChangeRequestedEvent → SyncBankAccountUseCase
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("DataChangeRequestedEvent → SyncBankAccountUseCase")
  class DataChangeRequestedTests {

    @Test
    @DisplayName("Debe parsear el payload JSON bancario y delegar al UC de sincronización")
    void shouldDelegateBankAccountSync() {
      // Arrange
      UUID actionId = UUID.randomUUID();
      UUID personId = UUID.randomUUID();
      UUID tenantId =  UUID.randomUUID();
      String payload =
          """
          {
            "bankAccountNumber": "1234567890",
            "bankCode": "BNB"
          }
          """;

      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              actionId, personId, "BANK_ACCOUNT_UPDATE", payload, tenantId, Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(syncBankAccountUseCase, times(1))
          .syncBankAccount(
              eq(personId),
              eq("1234567890"),
              eq("BNB"),
              eq(tenantId),
              eq(actionId.toString()));
    }

    @Test
    @DisplayName("Debe ignorar eventos que no son de tipo bancario")
    void shouldSkipNonBankAccountActionTypes() {
      // Arrange
      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              UUID.randomUUID(),
              UUID.randomUUID(),
              "DATA_UPDATE",
              "{}",
                  UUID.randomUUID(),
              Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(syncBankAccountUseCase, never())
          .syncBankAccount(any(), anyString(), anyString(),  UUID.randomUUID(), anyString());
    }

    @Test
    @DisplayName("Debe capturar la excepción si el payload JSON es inválido")
    void shouldCatchExceptionWhenPayloadIsInvalid() {
      // Arrange
      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              UUID.randomUUID(),
              UUID.randomUUID(),
              "BANK_ACCOUNT_UPDATE",
              "{ invalid json ",
              UUID.randomUUID(),
              Instant.now());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(syncBankAccountUseCase, never())
          .syncBankAccount(any(), anyString(), anyString(),  UUID.randomUUID(), anyString());
    }

    @Test
    @DisplayName("Debe rechazar payload bancario con campos vacíos sin invocar al UC")
    void shouldRejectPayloadWithMissingBankFields() {
      // Arrange
      String payload =
          """
          {
            "bankAccountNumber": "",
            "bankCode": ""
          }
          """;

      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              UUID.randomUUID(),
              UUID.randomUUID(),
              "BANK_ACCOUNT_UPDATE",
              payload,
                  UUID.randomUUID(),
              Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(syncBankAccountUseCase, never())
          .syncBankAccount(any(), anyString(), anyString(),  UUID.randomUUID(), anyString());
    }

    @Test
    @DisplayName("Debe capturar la excepción si SyncBankAccountUseCase falla")
    void shouldCatchExceptionWhenSyncFails() {
      // Arrange
      UUID actionId = UUID.randomUUID();
      UUID personId = UUID.randomUUID();
      String payload =
          """
          {
            "bankAccountNumber": "9876543210",
            "bankCode": "BCP"
          }
          """;

      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              actionId,
              personId,
              "BANK_ACCOUNT_UPDATE",
              payload,
                  UUID.randomUUID(),
              Instant.now());

      doThrow(new RuntimeException("Error de sincronización bancaria"))
          .when(syncBankAccountUseCase)
          .syncBankAccount(any(), anyString(), anyString(),  UUID.randomUUID(), anyString());

      // Act & Assert
      assertDoesNotThrow(() -> eventPublisher.publishEvent(event));

      verify(syncBankAccountUseCase, times(1))
          .syncBankAccount(
              eq(personId),
              eq("9876543210"),
              eq("BCP"),
              eq( UUID.randomUUID()),
              eq(actionId.toString()));
    }

    @Test
    @DisplayName("Debe aceptar variantes de casing en actionType bancario")
    void shouldAcceptCaseInsensitiveBankActionType() {
      // Arrange
      UUID actionId = UUID.randomUUID();
      UUID personId = UUID.randomUUID();
      String payload =
          """
          {
            "bankAccountNumber": "1111111111",
            "bankCode": "BISA"
          }
          """;

      DataChangeRequestedEvent event =
          new DataChangeRequestedEvent(
              actionId,
              personId,
              "bank_account_change",
              payload,
                  UUID.randomUUID(),
              Instant.now());

      // Act
      eventPublisher.publishEvent(event);

      // Assert
      verify(syncBankAccountUseCase, times(1))
          .syncBankAccount(
              eq(personId),
              eq("1111111111"),
              eq("BISA"),
              eq( UUID.randomUUID()),
              eq(actionId.toString()));
    }
  }
}
