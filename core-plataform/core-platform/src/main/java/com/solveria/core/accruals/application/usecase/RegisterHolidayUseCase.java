package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.RegisterHolidayCommand;
import com.solveria.core.accruals.domain.model.HolidayCalendar;

public interface RegisterHolidayUseCase {

  HolidayCalendar handle(RegisterHolidayCommand command);
}
