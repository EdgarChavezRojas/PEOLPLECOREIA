package com.solveria.core.financial.application.port;

import com.solveria.core.financial.domain.model.SocialSecurityAccount;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de SocialSecurityAccount. */
public interface SocialSecurityAccountRepositoryPort {

  Optional<SocialSecurityAccount> findById(UUID ssaId);

  Optional<SocialSecurityAccount> findByPersonId(UUID personId, UUID tenantId);

  void save(SocialSecurityAccount account);
}
