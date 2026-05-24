package com.solveria.TimeAndBearings.domain.model.ar;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.solveria.TimeAndBearings.domain.exception.GracePeriodActiveException;
import com.solveria.TimeAndBearings.domain.exception.TimesheetPeriodImmutableException;
import com.solveria.TimeAndBearings.domain.model.entity.DailyConsolidationSummary;
import com.solveria.TimeAndBearings.domain.model.enums.ClosureType;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodType;
import com.solveria.TimeAndBearings.domain.model.vo.PeriodBoundary;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimesheetPeriodTest {

  @Test
  void shouldThrowExceptionWhenGracePeriodStillActiveOnAutoClose() {
    // given
    LocalDate periodStart = LocalDate.of(2026, 5, 1);
    LocalDate periodEnd = LocalDate.of(2026, 5, 7);
    LocalDateTime gracePeriodEnd = LocalDateTime.of(2026, 5, 10, 0, 0);
    TimesheetPeriod period =
        TimesheetPeriod.open(
            UUID.randomUUID(),
            UUID.randomUUID(),
            periodStart,
            periodEnd,
            PeriodType.WEEKLY,
            gracePeriodEnd);
    Instant serverInstant = LocalDateTime.of(2026, 5, 9, 12, 0).toInstant(ZoneOffset.UTC);

    // when
    // then
    assertThatThrownBy(() -> period.autoClosePeriod(0, List.of(), List.of(), serverInstant))
        .isInstanceOf(GracePeriodActiveException.class)
        .hasMessageContaining("Periodo de Gracia");
  }

  @Test
  void shouldThrowExceptionWhenModifyingClosedPeriod() {
    // given
    UUID periodId = UUID.randomUUID();
    PeriodBoundary boundary =
        new PeriodBoundary(
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 7),
            PeriodType.WEEKLY,
            LocalDateTime.of(2026, 5, 10, 0, 0));
    TimesheetPeriod period =
        new TimesheetPeriod(
            periodId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            boundary,
            PeriodStatus.CLOSED,
            LocalDateTime.of(2026, 5, 10, 8, 0),
            UUID.randomUUID(),
            ClosureType.MANUAL,
            null,
            List.of(),
            null);
    DailyConsolidationSummary summary =
        DailyConsolidationSummary.create(
            periodId, LocalDate.of(2026, 5, 2), LocalDateTime.of(2026, 5, 2, 23, 0));

    // when
    // then
    assertThatThrownBy(() -> period.addOrUpdateDailySummary(summary))
        .isInstanceOf(TimesheetPeriodImmutableException.class)
        .hasMessageContaining("inmutable");
  }
}
