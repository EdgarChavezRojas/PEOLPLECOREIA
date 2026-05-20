package com.solveria.TimeAndBearings.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solveria.TimeAndBearings.application.command.ClosePeriodManuallyCommand;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerConsolidationPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.application.port.outbound.TimesheetPeriodRepositoryPort;
import com.solveria.TimeAndBearings.domain.event.AttendancePeriodClosedEvent;
import com.solveria.TimeAndBearings.domain.exception.PendingLedgersBlockClosureException;
import com.solveria.TimeAndBearings.domain.model.ar.TimesheetPeriod;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodType;
import com.solveria.core.shared.events.DomainEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimesheetConsolidationUseCaseTest {

  @Mock private TimesheetPeriodRepositoryPort periodRepository;

  @Mock private AttendanceLedgerConsolidationPort ledgerConsolidationPort;

  @Mock private EventOutboxPort outbox;

  @Test
  void shouldBlockManualCloseWhenPendingLedgersExistPerPolicyPTM33() {
    // given
    Clock clock = Clock.fixed(Instant.parse("2026-05-05T12:00:00Z"), ZoneOffset.UTC);
    TimesheetConsolidationUseCase useCase =
        new TimesheetConsolidationUseCase(periodRepository, ledgerConsolidationPort, outbox, clock);

    UUID tenantId = UUID.randomUUID();
    UUID orgUnitId = UUID.randomUUID();
    LocalDate periodStart = LocalDate.of(2026, 5, 1);
    LocalDate periodEnd = LocalDate.of(2026, 5, 7);
    LocalDateTime graceEnd = LocalDateTime.of(2026, 5, 11, 17, 0);
    TimesheetPeriod period =
        TimesheetPeriod.open(
            tenantId, orgUnitId, periodStart, periodEnd, PeriodType.WEEKLY, graceEnd);

    when(periodRepository.findById(eq(period.getPeriodId()))).thenReturn(Optional.of(period));
    when(periodRepository.countNonClosedLedgers(
            eq(period.getPeriodId()), eq(periodStart), eq(periodEnd)))
        .thenReturn(2);
    when(ledgerConsolidationPort.computeEmployeePeriodSummaries(
            eq(orgUnitId), eq(periodStart), eq(periodEnd)))
        .thenReturn(List.of());

    ClosePeriodManuallyCommand command =
        new ClosePeriodManuallyCommand(period.getPeriodId(), UUID.randomUUID());

    // when / then
    assertThatThrownBy(() -> useCase.closePeriodManually(command))
        .isInstanceOf(PendingLedgersBlockClosureException.class);

    verify(periodRepository, never()).save(any(TimesheetPeriod.class));
    verify(outbox, never()).store(anyList());
  }

  @Test
  void shouldAutoCloseExpiredGracePeriodsAndPublishAttendancePeriodClosedEvent() {
    // given
    Instant serverInstant = Instant.parse("2026-05-05T17:10:00Z");
    Clock clock = Clock.fixed(serverInstant, ZoneOffset.UTC);
    TimesheetConsolidationUseCase useCase =
        new TimesheetConsolidationUseCase(periodRepository, ledgerConsolidationPort, outbox, clock);

    UUID tenantId = UUID.randomUUID();
    UUID orgUnitId = UUID.randomUUID();
    LocalDate periodStart = LocalDate.of(2026, 4, 21);
    LocalDate periodEnd = LocalDate.of(2026, 4, 30);
    LocalDateTime graceEnd = LocalDateTime.ofInstant(serverInstant, ZoneOffset.UTC).minusDays(1);
    TimesheetPeriod period =
        TimesheetPeriod.open(
            tenantId, orgUnitId, periodStart, periodEnd, PeriodType.MONTHLY, graceEnd);

    when(periodRepository.findExpiredGracePeriods(any(LocalDateTime.class)))
        .thenReturn(List.of(period));
    when(ledgerConsolidationPort.forceAutoClosePendingLedgers(
            eq(period.getPeriodId()), eq(periodStart), eq(periodEnd), any(LocalDateTime.class)))
        .thenReturn(1);
    when(periodRepository.countNonClosedLedgers(
            eq(period.getPeriodId()), eq(periodStart), eq(periodEnd)))
        .thenReturn(0);
    when(ledgerConsolidationPort.computeEmployeePeriodSummaries(
            eq(orgUnitId), eq(periodStart), eq(periodEnd)))
        .thenReturn(List.of());
    when(periodRepository.save(any(TimesheetPeriod.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    useCase.evaluateAndExecuteGracePeriodClosure();

    // then
    assertThat(period.getStatus()).isEqualTo(PeriodStatus.CLOSED);
    verify(periodRepository).save(period);

    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(outbox).store(eventsCaptor.capture());
    assertThat(eventsCaptor.getValue())
        .anyMatch(event -> event instanceof AttendancePeriodClosedEvent);
  }
}
