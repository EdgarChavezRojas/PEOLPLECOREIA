package com.solveria.core.accruals.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.solveria.core.accruals.domain.event.AccrualEvent;
import com.solveria.core.accruals.domain.event.AccrualEventType;
import com.solveria.core.accruals.domain.exception.InsufficientAccrualBalanceException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccrualBalanceTest {

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

    boolean hasThresholdEvent =
        balance.pullDomainEvents().stream()
            .filter(AccrualEvent.class::isInstance)
            .map(AccrualEvent.class::cast)
            .anyMatch(event -> event.type() == AccrualEventType.VACATION_BALANCE_THRESHOLD_LOW);

    if (!hasThresholdEvent) {
      throw new AssertionError("Expected VACATION_BALANCE_THRESHOLD_LOW event");
    }
  }
}
