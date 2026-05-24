package com.solveria.core.accruals.domain.policy;

import com.solveria.core.accruals.domain.exception.InvalidAccrualStateException;

public final class LocalizationPolicy {

  public static final String SANTA_CRUZ_BOLIVIA = "Santa Cruz, Bolivia";

  private LocalizationPolicy() {}

  public static void requireSantaCruz(String location) {
    if (location == null || !SANTA_CRUZ_BOLIVIA.equalsIgnoreCase(location.trim())) {
      throw new InvalidAccrualStateException("invalid location: " + location);
    }
  }
}
