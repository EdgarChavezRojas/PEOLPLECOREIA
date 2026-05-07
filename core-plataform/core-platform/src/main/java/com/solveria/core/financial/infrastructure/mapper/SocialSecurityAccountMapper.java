package com.solveria.core.financial.infrastructure.mapper;

import com.solveria.core.financial.domain.model.SocialSecurityAccount;
import com.solveria.core.financial.infrastructure.jpa.SocialSecurityAccountJpa;
import org.mapstruct.Mapper;

/** MapStruct Mapper: SocialSecurityAccount Domain ↔ JPA. */
@Mapper(componentModel = "spring")
public interface SocialSecurityAccountMapper {

  default SocialSecurityAccountJpa toJpa(SocialSecurityAccount account) {
    if (account == null) {
      return null;
    }
    SocialSecurityAccountJpa jpa = new SocialSecurityAccountJpa();
    jpa.setSsaId(account.getSsaId());
    jpa.setPersonId(account.getPersonId());
    jpa.setGestoraCode(account.getGestoraCode());
    jpa.setContributionRate(account.getContributionRateValue());
    jpa.setLastContribution(account.getLastContribution());
    jpa.setTenantId(account.getTenantId());
    jpa.setCreatedByUser(account.getCreatedBy());
    return jpa;
  }

  default SocialSecurityAccount toDomain(SocialSecurityAccountJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return SocialSecurityAccount.rehydrate(
        jpa.getSsaId(),
        jpa.getPersonId(),
        jpa.getGestoraCode(),
        jpa.getContributionRate(),
        jpa.getLastContribution(),
        jpa.getTenantId(),
        jpa.getCreatedByUser());
  }
}
