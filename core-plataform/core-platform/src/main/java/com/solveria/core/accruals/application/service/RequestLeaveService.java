package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RequestLeaveCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.RequestLeaveUseCase; // Interfaz necesaria
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.HolidayPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestLeaveService implements RequestLeaveUseCase { // IMPLEMENTA LA INTERFAZ

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;
  private final BenefitsRepositoryPort benefitsRepository;

  public RequestLeaveService(
      AccrualBalanceRepositoryPort accrualBalanceRepository,
      BenefitsRepositoryPort benefitsRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  @Transactional
  public AccrualBalance handle(RequestLeaveCommand command) {
    // Obtenemos el tenant del contexto de seguridad
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new RuntimeException("Balance no encontrado"));

    // Inyectamos el tenant al dominio
    balance.setTenantId(tenantId);

    var holidays =
        benefitsRepository.findHolidaysBetween(command.startDate(), command.endDate(), tenantId);
    var days =
        HolidayPolicy.calculateChargeableDays(command.startDate(), command.endDate(), holidays);

    // La lógica interna de AccrualBalance ahora usa el tenantId correctamente
    balance.requestLeave(command.startDate(), command.endDate(), days);

    return accrualBalanceRepository.save(balance);
  }
}
