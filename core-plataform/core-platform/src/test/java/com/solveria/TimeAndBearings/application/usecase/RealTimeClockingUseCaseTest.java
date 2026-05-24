package com.solveria.TimeAndBearings.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solveria.TimeAndBearings.application.command.ClockCommand;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.event.PunchAnomalyDetectedEvent;
import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealTimeClockingUseCaseTest {

  @Mock private AttendanceLedgerRepositoryPort ledgerRepository;

  @Mock private EventOutboxPort eventOutbox;

  @Test
  void shouldPersistEntryRegisterDeviationAndPublishEventWhenOutsideFence() {
    // given
    RealTimeClockingUseCase useCase = new RealTimeClockingUseCase(ledgerRepository, eventOutbox);
    UUID tenantId = UUID.randomUUID();
    UUID relationshipId = UUID.randomUUID();
    ClockCommand command =
        new ClockCommand(
            tenantId,
            relationshipId,
            PunchSource.MOBILE,
            null,
            null,
            "10.0.0.1",
            "JUnit",
            new BigDecimal("-17.7833"),
            new BigDecimal("-63.1821"),
            new BigDecimal("5.0"),
            null,
            null,
            false);
    when(ledgerRepository.findByRelationshipAndDate(
            eq(tenantId), eq(relationshipId), any(LocalDate.class)))
        .thenReturn(Optional.empty());
    when(ledgerRepository.save(any(AttendanceLedger.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    TimeEntry entry = useCase.clock(command);

    // then
    ArgumentCaptor<AttendanceLedger> ledgerCaptor = ArgumentCaptor.forClass(AttendanceLedger.class);
    verify(ledgerRepository).save(ledgerCaptor.capture());
    verify(eventOutbox).store(anyList());

    AttendanceLedger savedLedger = ledgerCaptor.getValue();
    assertThat(savedLedger.getTimeEntries()).hasSize(1);
    assertThat(savedLedger.getTimeEntries().getFirst().getGeoSnapshot().geoStatus())
        .isEqualTo(GeoStatus.OUTSIDE_FENCE);
    assertThat(savedLedger.getDeviations()).hasSize(1);
    assertThat(savedLedger.getDeviations().getFirst().getDeviationType())
        .isEqualTo(DeviationType.GEO_VIOLATION);

    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventOutbox).store(eventsCaptor.capture());
    assertThat(eventsCaptor.getValue())
        .anyMatch(event -> event instanceof PunchAnomalyDetectedEvent);
    assertThat(entry).isNotNull();
  }
}
