package com.solveria.core.accruals.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.solveria.core.accruals.domain.event.VacationBalanceThresholdLowEvent;
import com.solveria.core.accruals.domain.exception.InsufficientAccrualBalanceException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class  AccrualBalanceTest {

  @Test
  void requestLeaveEmitsThresholdEventWhenInsufficientBalance() {
    AccrualBalance balance =
        AccrualBalance.open(
            UUID.randomUUID(),
            AccrualBalanceType.VACATION,
            AccrualUnit.DAYS,
            BigDecimal.valueOf(5),
            LocalDate.now(),
            UUID.randomUUID());

    assertThrows(
        InsufficientAccrualBalanceException.class,
        () ->
            balance.requestLeave(
                LocalDate.now(), LocalDate.now().plusDays(1), BigDecimal.valueOf(6)));

    VacationBalanceThresholdLowEvent event =
        balance.pullDomainEvents().stream()
            .filter(VacationBalanceThresholdLowEvent.class::isInstance)
            .map(VacationBalanceThresholdLowEvent.class::cast)
            .findFirst()
            .orElse(null);

    assertNotNull(event, "Expected VacationBalanceThresholdLowEvent");
    assertEquals(balance.getBalanceId(), event.balanceId());
    assertEquals(0, event.requestedDays().compareTo(BigDecimal.valueOf(6)));
    assertEquals(0, event.currentBalance().compareTo(BigDecimal.valueOf(5)));
  }
}
