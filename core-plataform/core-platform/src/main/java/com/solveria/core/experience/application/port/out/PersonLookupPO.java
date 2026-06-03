package com.solveria.core.experience.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Consulta de persona por userId del JWT. */
public interface PersonLookupPO {

  Optional<UUID> findPersonIdByUserId(Long userId);
}
