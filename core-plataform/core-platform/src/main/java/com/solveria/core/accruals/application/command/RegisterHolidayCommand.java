package com.solveria.core.accruals.application.command;

import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import java.time.LocalDate;

public record RegisterHolidayCommand(LocalDate holidayDate, HolidayScope scope, String location) {}
