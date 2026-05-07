package com.solveria.core.accruals.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import com.solveria.core.accruals.domain.policy.HolidayPolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HolidayPolicyTest {

  @Test
  void calculateChargeableDaysExcludesSantaCruzDay() {
    LocalDate startDate = LocalDate.of(2026, 9, 23);
    LocalDate endDate = LocalDate.of(2026, 9, 24);

    HolidayCalendar holiday =
        HolidayCalendar.register(
            LocalDate.of(2026, 1, 1), HolidayScope.NATIONAL, UUID.randomUUID());

    BigDecimal chargeable =
        HolidayPolicy.calculateChargeableDays(startDate, endDate, List.of(holiday));

    assertEquals(BigDecimal.ONE, chargeable);
  }
}
