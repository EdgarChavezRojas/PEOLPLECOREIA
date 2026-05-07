package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCalendar {

  private UUID holidayId;
  private LocalDate holidayDate;
  private HolidayScope scope;
  private UUID tenantId;

  public static HolidayCalendar register(LocalDate holidayDate, HolidayScope scope, UUID tenantId) {
    if (holidayDate == null) {
      throw new IllegalArgumentException("holidayDate is required");
    }
    if (scope == null) {
      throw new IllegalArgumentException("scope is required");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId is required");
    }
    return HolidayCalendar.builder()
        .holidayId(UUID.randomUUID())
        .holidayDate(holidayDate)
        .scope(scope)
        .tenantId(tenantId)
        .build();
  }

  public boolean appliesTo(LocalDate date) {
    if (date == null || holidayDate == null) {
      return false;
    }
    return holidayDate.equals(date);
  }
}
