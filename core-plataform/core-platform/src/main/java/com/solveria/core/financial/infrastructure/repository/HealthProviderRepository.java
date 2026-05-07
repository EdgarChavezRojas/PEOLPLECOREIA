package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.jpa.HealthProviderJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: HealthProvider. */
@Repository
public interface HealthProviderRepository extends JpaRepository<HealthProviderJpa, Long> {

  Optional<HealthProviderJpa> findByProviderIdAndTenantId(UUID providerId, String tenantId);
}
