package com.solveria.core.accruals.domain.policy;

public final class VacationScalePolicy {

  private VacationScalePolicy() {}

  public static int vacationDaysForYears(int yearsOfService) {
    if (yearsOfService <= 0) {
      return 0;
    }
    if (yearsOfService <= 5) {
      return 15;
    }
    if (yearsOfService <= 10) {
      return 20;
    }
    return 30;
  }
}
