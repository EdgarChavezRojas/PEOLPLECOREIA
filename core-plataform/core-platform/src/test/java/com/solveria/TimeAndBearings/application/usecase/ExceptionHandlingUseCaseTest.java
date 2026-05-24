package com.solveria.TimeAndBearings.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solveria.TimeAndBearings.application.command.ResolveDeviationCommand;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.event.ExceptionAutoClosedEvent;
import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import com.solveria.TimeAndBearings.domain.model.vo.ExceptionAuditEntry;
import com.solveria.core.shared.events.DomainEvent;
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
class ExceptionHandlingUseCaseTest {

  @Mock private AttendanceLedgerRepositoryPort ledgerRepository;

  @Mock private EventOutboxPort eventOutbox;

  @Test
  void shouldRecordAuditEntryWhenManagerOverridesRetroactivePunchWithValidReasonNote() {
    // given
    ExceptionHandlingUseCase useCase = new ExceptionHandlingUseCase(ledgerRepository, eventOutbox);
    UUID tenantId = UUID.randomUUID();
    UUID relationshipId = UUID.randomUUID();
    AttendanceLedger ledger =
        AttendanceLedger.open(
            tenantId,
            relationshipId,
            LocalDate.now(),
            UUID.randomUUID(),
            LocalDateTime.now(ZoneOffset.UTC));
    TimeDeviationRecord deviation =
        ledger.registerDeviation(
            DeviationType.MISSING_PUNCH, 10, LocalDateTime.now(ZoneOffset.UTC).minusHours(3));
    when(ledgerRepository.findById(eq(ledger.getLedgerId()))).thenReturn(Optional.of(ledger));
    when(ledgerRepository.save(any(AttendanceLedger.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ResolveDeviationCommand command =
        new ResolveDeviationCommand(
            ledger.getLedgerId(),
            deviation.getDeviationId(),
            UUID.randomUUID(),
            ResolutionStatus.OVERRIDDEN_BY_MANAGER,
            "Justificacion suficiente para override",
            UUID.randomUUID(),
            tenantId);

    // when
    TimeDeviationRecord resolved = useCase.resolveDeviation(command);

    // then
    assertThat(resolved.getResolutionStatus()).isEqualTo(ResolutionStatus.OVERRIDDEN_BY_MANAGER);
    assertThat(resolved.getAuditTrail()).hasSize(1);
    ExceptionAuditEntry auditEntry = resolved.getAuditTrail().getFirst();
    assertThat(auditEntry.reasonNote()).hasSizeGreaterThanOrEqualTo(20);
    verify(ledgerRepository).save(ledger);
    verify(eventOutbox).store(anyList());
  }

  @Test
  void shouldRejectRetroactivePunchOverrideWhenReasonNoteTooShort() {
    // given
    ExceptionHandlingUseCase useCase = new ExceptionHandlingUseCase(ledgerRepository, eventOutbox);
    UUID tenantId = UUID.randomUUID();
    UUID relationshipId = UUID.randomUUID();
    AttendanceLedger ledger =
        AttendanceLedger.open(
            tenantId,
            relationshipId,
            LocalDate.now(),
            UUID.randomUUID(),
            LocalDateTime.now(ZoneOffset.UTC));
    TimeDeviationRecord deviation =
        ledger.registerDeviation(
            DeviationType.MISSING_PUNCH, 10, LocalDateTime.now(ZoneOffset.UTC).minusHours(3));
    when(ledgerRepository.findById(eq(ledger.getLedgerId()))).thenReturn(Optional.of(ledger));

    ResolveDeviationCommand command =
        new ResolveDeviationCommand(
            ledger.getLedgerId(),
            deviation.getDeviationId(),
            UUID.randomUUID(),
            ResolutionStatus.OVERRIDDEN_BY_MANAGER,
            "Demasiado corta",
            UUID.randomUUID(),
            tenantId);

    // when / then
    assertThatThrownBy(() -> useCase.resolveDeviation(command))
        .isInstanceOf(IllegalArgumentException.class);

    verify(ledgerRepository, never()).save(any(AttendanceLedger.class));
    verify(eventOutbox, never()).store(anyList());
  }

  @Test
  void shouldAutoCloseExpiredDeviationsAndPublishEventWhenJustificationWindowExpires() {
    // given
    ExceptionHandlingUseCase useCase = new ExceptionHandlingUseCase(ledgerRepository, eventOutbox);
    UUID tenantId = UUID.randomUUID();
    UUID relationshipId = UUID.randomUUID();
    AttendanceLedger ledger =
        AttendanceLedger.open(
            tenantId,
            relationshipId,
            LocalDate.now(),
            UUID.randomUUID(),
            LocalDateTime.now(ZoneOffset.UTC));
    TimeDeviationRecord deviation =
        ledger.registerDeviation(
            DeviationType.NO_SHOW, 480, LocalDateTime.now(ZoneOffset.UTC).minusHours(80));

    when(ledgerRepository.findLedgersWithExpiredPendingDeviations(
            eq(tenantId), any(LocalDateTime.class)))
        .thenReturn(List.of(ledger));
    when(ledgerRepository.save(any(AttendanceLedger.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    useCase.autoCloseExpiredDeviations(tenantId);

    // then
    assertThat(deviation.getResolutionStatus())
        .isEqualTo(ResolutionStatus.AUTO_CLOSED_AS_UNJUSTIFIED);
    verify(ledgerRepository).save(ledger);

    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventOutbox).store(eventsCaptor.capture());
    assertThat(eventsCaptor.getValue())
        .anyMatch(event -> event instanceof ExceptionAutoClosedEvent);
  }
}
