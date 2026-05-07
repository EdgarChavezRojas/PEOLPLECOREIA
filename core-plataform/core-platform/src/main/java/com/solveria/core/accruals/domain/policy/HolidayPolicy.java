package com.solveria.core.accruals.domain.policy;

import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HolidayPolicy {

  private static final MonthDay SANTA_CRUZ_DAY = MonthDay.of(9, 24);

  private HolidayPolicy() {}

  public static BigDecimal calculateChargeableDays(
      LocalDate startDate, LocalDate endDate, List<HolidayCalendar> holidays) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("startDate and endDate are required");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("endDate must be after startDate");
    }
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    long holidayDays = countHolidaysInRange(startDate, endDate, holidays);
    long chargeable = Math.max(0, totalDays - holidayDays);
    return BigDecimal.valueOf(chargeable);
  }

  public static boolean isHoliday(LocalDate date, List<HolidayCalendar> holidays) {
    if (date == null) {
      return false;
    }
    if (SANTA_CRUZ_DAY.equals(MonthDay.from(date))) {
      return true;
    }
    if (holidays == null || holidays.isEmpty()) {
      return false;
    }
    return holidays.stream().anyMatch(holiday -> holiday.appliesTo(date));
  }

  private static long countHolidaysInRange(
      LocalDate startDate, LocalDate endDate, List<HolidayCalendar> holidays) {
    Set<LocalDate> uniqueHolidays = new HashSet<>();
    LocalDate cursor = startDate;
    while (!cursor.isAfter(endDate)) {
      if (SANTA_CRUZ_DAY.equals(MonthDay.from(cursor))) {
        uniqueHolidays.add(cursor);
      }
      cursor = cursor.plusDays(1);
    }
    if (holidays == null || holidays.isEmpty()) {
      return uniqueHolidays.size();
    }
    for (HolidayCalendar holiday : holidays) {
      if (holiday == null || holiday.getHolidayDate() == null) {
        continue;
      }
      if (holiday.getScope() == HolidayScope.NATIONAL
          || holiday.getScope() == HolidayScope.REGIONAL_SCZ) {
        if (!holiday.getHolidayDate().isBefore(startDate)
            && !holiday.getHolidayDate().isAfter(endDate)) {
          uniqueHolidays.add(holiday.getHolidayDate());
        }
      }
    }
    return uniqueHolidays.size();
  }
}
