package com.solveria.core.financial.infrastructure.mapper;

import com.solveria.core.financial.domain.model.HealthProvider;
import com.solveria.core.financial.infrastructure.jpa.HealthProviderJpa;
import org.mapstruct.Mapper;

/** MapStruct Mapper: HealthProvider Domain ↔ JPA. */
@Mapper(componentModel = "spring")
public interface HealthProviderMapper {

  default HealthProviderJpa toJpa(HealthProvider provider) {
    if (provider == null) {
      return null;
    }
    HealthProviderJpa jpa = new HealthProviderJpa();
    jpa.setProviderId(provider.getProviderId());
    jpa.setRegistrationNo(provider.getRegistrationNo());
    jpa.setStatus(provider.getStatus());
    jpa.setTenantId(provider.getTenantId());
    jpa.setCreatedByUser(provider.getCreatedBy());
    return jpa;
  }

  default HealthProvider toDomain(HealthProviderJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return HealthProvider.rehydrate(
        jpa.getProviderId(),
        jpa.getRegistrationNo(),
        jpa.getStatus(),
        jpa.getTenantId(),
        jpa.getCreatedByUser());
  }
}
