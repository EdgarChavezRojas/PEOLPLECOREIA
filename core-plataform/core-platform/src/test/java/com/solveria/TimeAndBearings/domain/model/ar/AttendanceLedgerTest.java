package com.solveria.TimeAndBearings.domain.model.ar;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.solveria.TimeAndBearings.domain.exception.ActivePunchAlreadyExistsException;
import com.solveria.TimeAndBearings.domain.exception.AttendanceClosureParityException;
import com.solveria.TimeAndBearings.domain.exception.ChronologicalIntegrityException;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.TimeAndBearings.domain.model.enums.PunchType;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import com.solveria.TimeAndBearings.domain.model.vo.WorkedHoursSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AttendanceLedgerTest {

    @Test
    void shouldThrowExceptionWhenActivePunchUniquenessViolated() {
        // given
        UUID tenantId = UUID.randomUUID();
        UUID relationshipId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 8, 0);
        AttendanceLedger ledger = AttendanceLedger.open(
                tenantId,
                relationshipId,
                baseTime.toLocalDate(),
                null,
                baseTime);
        PunchContext context = new PunchContext(null, PunchSource.MOBILE, "10.0.0.1", "JUnit");
        GeoValidationSnapshot geoSnapshot = GeoValidationSnapshot.noGps();
        ledger.recordPunch(baseTime, PunchType.PUNCH_IN, context, geoSnapshot, null, false);

        // when
        // then
        assertThatThrownBy(() -> ledger.recordPunch(
                baseTime.plusMinutes(1),
                PunchType.PUNCH_IN,
                context,
                geoSnapshot,
                null,
                false))
                .isInstanceOf(ActivePunchAlreadyExistsException.class)
                .hasMessageContaining("Active Punch Uniqueness");
    }

    @Test
    void shouldThrowExceptionWhenPunchTimeInFutureBeyondNtpTolerance() {
        // given
        UUID tenantId = UUID.randomUUID();
        UUID relationshipId = UUID.randomUUID();
        LocalDate workDate = LocalDate.now(ZoneOffset.UTC);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 8, 0);
        AttendanceLedger ledger = AttendanceLedger.open(tenantId, relationshipId, workDate, null, createdAt);
        PunchContext context = new PunchContext(null, PunchSource.MOBILE, "10.0.0.1", "JUnit");
        GeoValidationSnapshot geoSnapshot = GeoValidationSnapshot.noGps();
        LocalDateTime futureTime = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(10);

        // when
        // then
        assertThatThrownBy(() -> ledger.recordPunch(
                futureTime,
                PunchType.PUNCH_IN,
                context,
                geoSnapshot,
                null,
                false))
                .isInstanceOf(ChronologicalIntegrityException.class)
                .hasMessageContaining("punch_time is in the future");
    }

    @Test
    void shouldThrowExceptionWhenClosingWithUnpairedPunchesWithoutOverride() {
        // given
        UUID tenantId = UUID.randomUUID();
        UUID relationshipId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 8, 0);
        AttendanceLedger ledger = AttendanceLedger.open(
                tenantId,
                relationshipId,
                baseTime.toLocalDate(),
                null,
                baseTime);
        PunchContext context = new PunchContext(null, PunchSource.MOBILE, "10.0.0.1", "JUnit");
        GeoValidationSnapshot geoSnapshot = GeoValidationSnapshot.noGps();
        ledger.recordPunch(baseTime, PunchType.PUNCH_IN, context, geoSnapshot, null, false);
        ledger.setWorkedHoursSummary(new WorkedHoursSummary(
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                BigDecimal.ONE,
                baseTime.plusHours(10)));

        // when
        // then
        assertThatThrownBy(() -> ledger.close(baseTime.plusHours(12)))
                .isInstanceOf(AttendanceClosureParityException.class)
                .hasMessageContaining("Closure Parity");
    }
}

