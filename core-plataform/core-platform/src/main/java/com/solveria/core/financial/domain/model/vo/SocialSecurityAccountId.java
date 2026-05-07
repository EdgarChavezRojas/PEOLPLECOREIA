package com.solveria.core.financial.domain.model.vo;

import java.util.UUID;

/** VO: Identidad tipada para SocialSecurityAccount. */
public record SocialSecurityAccountId(UUID value) {

  public SocialSecurityAccountId {
    if (value == null) {
      throw new IllegalArgumentException("SocialSecurityAccountId no puede ser null");
    }
  }

  public static SocialSecurityAccountId generate() {
    return new SocialSecurityAccountId(UUID.randomUUID());
  }

  public static SocialSecurityAccountId of(UUID value) {
    return new SocialSecurityAccountId(value);
  }
}
