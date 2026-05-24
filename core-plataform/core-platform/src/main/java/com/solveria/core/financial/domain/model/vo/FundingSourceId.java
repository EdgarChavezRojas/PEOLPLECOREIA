package com.solveria.core.financial.domain.model.vo;

import java.util.UUID;

/** VO: Identidad tipada para FundingSource. */
public record FundingSourceId(UUID value) {

  public FundingSourceId {
    if (value == null) {
      throw new IllegalArgumentException("FundingSourceId no puede ser null");
    }
  }

  public static FundingSourceId generate() {
    return new FundingSourceId(UUID.randomUUID());
  }

  public static FundingSourceId of(UUID value) {
    return new FundingSourceId(value);
  }
}
