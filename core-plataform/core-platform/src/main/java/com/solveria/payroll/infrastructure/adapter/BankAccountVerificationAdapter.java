package com.solveria.payroll.infrastructure.adapter;

import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.payroll.application.port.outbound.BankAccountVerificationPort;
import com.solveria.payroll.application.port.outbound.PaymentMethodRepositoryPort;
import com.solveria.payroll.domain.model.entity.PaymentMethod;
import com.solveria.payroll.domain.model.vo.PaymentChannel;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankAccountVerificationAdapter implements BankAccountVerificationPort {

  private final RelationshipRepositoryPort relationshipRepository;
  private final PaymentMethodRepositoryPort paymentMethodRepository;

  @Override
  public boolean isBankAccountValidated(UUID employeeId, UUID tenantId) {
    if (employeeId == null || tenantId == null) {
      return false;
    }

    // 1. Consultar el vínculo laboral real (Relationship) del colaborador en Workforce
    Optional<Relationship> relationshipOpt =
        relationshipRepository.findByRelationshipIdAndTenantId(employeeId, tenantId);

    if (relationshipOpt.isEmpty()) {
      log.warn(
          "action=VALIDATE_BANK_ACCOUNT status=NOT_FOUND employeeId={} tenantId={}",
          employeeId,
          tenantId);
      return false;
    }

    Relationship relationship = relationshipOpt.get();

    // Guardrail operacional: Si el empleado no está activo, se rechaza la verificación bancaria
    // síncrona
    if (!RelationshipStatus.ACTIVE.equals(relationship.getCurrentStatus())) {
      log.warn(
          "action=VALIDATE_BANK_ACCOUNT status=INACTIVE_RELATIONSHIP employeeId={}", employeeId);
      return false;
    }

    // 2. Consultar el método de pago por defecto configurado para la dispersión del Tenant
    Optional<PaymentMethod> defaultPaymentMethodOpt = paymentMethodRepository.findDefault(tenantId);

    if (defaultPaymentMethodOpt.isPresent()) {
      PaymentMethod paymentMethod = defaultPaymentMethodOpt.get();

      // Si el canal configurado por la empresa es EFECTIVO o CHEQUE, por ley no se exige cuenta
      // bancaria
      if (PaymentChannel.CASH.equals(paymentMethod.getChannel())
          || PaymentChannel.CHEQUE.equals(paymentMethod.getChannel())) {
        return true;
      }
    }

    // 3. Validación de Negocio: Verificar condiciones del perfil del trabajador
    // (WorkerProfile/Condition)
    // Se extrae la condición de empleo y se comprueba que no existan bloqueos operativos en sus
    // logs de auditoría
    if (relationship.getEmploymentCondition() != null) {
      // Si posee una condición configurada, validamos que su cuenta no esté en un log de error
      // transaccional
      boolean hasBankingError =
          relationship.getStatusLogs().stream()
              .anyMatch(log -> "BANK_ACCOUNT_REJECTED".equals(log.getChangeReason()));

      if (hasBankingError) {
        log.error(
            "action=VALIDATE_BANK_ACCOUNT status=BLOCKED_BY_AUDIT_LOG employeeId={}", employeeId);
        return false;
      }
    }

    // Al no haber bloqueos operacionales en las tablas de negocio, la cuenta está apta para
    // dispersión
    return true;
  }
}
