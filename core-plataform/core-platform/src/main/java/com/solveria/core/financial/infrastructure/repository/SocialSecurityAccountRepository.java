package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.jpa.SocialSecurityAccountJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: SocialSecurityAccount. */
@Repository
public interface SocialSecurityAccountRepository
    extends JpaRepository<SocialSecurityAccountJpa, Long> {

  Optional<SocialSecurityAccountJpa> findBySsaIdAndTenantId(UUID ssaId, String tenantId);

  Optional<SocialSecurityAccountJpa> findByPersonIdAndTenantId(UUID personId, String tenantId);
}
