package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RequestLeaveCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.RequestLeaveUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.policy.HolidayPolicy;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RequestLeaveService implements RequestLeaveUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;
  private final BenefitsRepositoryPort benefitsRepository;

  public RequestLeaveService(
      AccrualBalanceRepositoryPort accrualBalanceRepository,
      BenefitsRepositoryPort benefitsRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public AccrualBalance handle(RequestLeaveCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));

    LocalDate startDate = command.startDate();
    LocalDate endDate = command.endDate();
    List<HolidayCalendar> holidays = benefitsRepository.findHolidaysBetween(startDate, endDate);
    BigDecimal chargeableDays = HolidayPolicy.calculateChargeableDays(startDate, endDate, holidays);

    balance.requestLeave(startDate, endDate, chargeableDays);
    return accrualBalanceRepository.save(balance);
  }
}
