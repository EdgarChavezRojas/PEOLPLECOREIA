package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RegisterHolidayCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.RegisterHolidayUseCase;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class RegisterHolidayService implements RegisterHolidayUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public RegisterHolidayService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public HolidayCalendar handle(RegisterHolidayCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    HolidayCalendar holiday =
        HolidayCalendar.register(command.holidayDate(), command.scope(), tenantId);
    return benefitsRepository.saveHoliday(holiday);
  }
}
