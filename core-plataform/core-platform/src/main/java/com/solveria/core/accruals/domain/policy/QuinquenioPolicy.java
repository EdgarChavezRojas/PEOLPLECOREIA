package com.solveria.core.accruals.domain.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class QuinquenioPolicy {

  /** Multa del 30% según normativa boliviana (Art. 9 DS 28699). */
  public static final BigDecimal PENALTY_RATE = new BigDecimal("0.30");

  private QuinquenioPolicy() {}

  public static boolean isEligible(int monthsCompleted) {
    return monthsCompleted >= 60;
  }

  public static boolean isPaymentOverdue(
      LocalDate requestDate, LocalDate today, LocalDate paymentDate) {
    if (requestDate == null || today == null) {
      return false;
    }
    if (paymentDate != null) {
      return false;
    }
    return today.isAfter(requestDate.plusDays(30));
  }
}
