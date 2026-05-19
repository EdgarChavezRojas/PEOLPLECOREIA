package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import java.time.LocalDate;
import java.util.UUID;

public class HolidayCalendar {

  private UUID holidayId;
  private LocalDate holidayDate;
  private HolidayScope scope;
  private UUID tenantId;

  public HolidayCalendar() {
  }

  public HolidayCalendar(UUID holidayId, LocalDate holidayDate, HolidayScope scope, UUID tenantId) {
    this.holidayId = holidayId;
    this.holidayDate = holidayDate;
    this.scope = scope;
    this.tenantId = tenantId;
  }

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
    return new HolidayCalendar(
            UUID.randomUUID(),
            holidayDate,
            scope,
            tenantId
    );
  }

  public boolean appliesTo(LocalDate date) {
    if (date == null || holidayDate == null) {
      return false;
    }
    return holidayDate.equals(date);
  }

  // Getters
  public UUID getHolidayId() { return holidayId; }
  public LocalDate getHolidayDate() { return holidayDate; }
  public HolidayScope getScope() { return scope; }
  public UUID getTenantId() { return tenantId; }
}