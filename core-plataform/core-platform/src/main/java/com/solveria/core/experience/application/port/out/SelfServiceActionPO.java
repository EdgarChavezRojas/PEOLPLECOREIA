package com.solveria.core.experience.application.port.out;

import com.solveria.core.experience.domain.model.SelfServiceAction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de SelfServiceAction. */
public interface SelfServiceActionPO {

  void save(SelfServiceAction action);

  Optional<SelfServiceAction> findById(UUID actionId);

  List<SelfServiceAction> findByPersonId(UUID personId);
}
