package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Person;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResolveDeduplicationUseCase {

  private static final String PERSON_NOT_FOUND = "PERSON_NOT_FOUND";

  private final PersonRepositoryPort personRepositoryPort;

  public void execute(UUID principalId, UUID duplicateId) {
    Person principal =
        personRepositoryPort
            .findByPersonId(principalId)
            .orElseThrow(() -> new SolverException(PERSON_NOT_FOUND));

    Person duplicate =
        personRepositoryPort
            .findByPersonId(duplicateId)
            .orElseThrow(() -> new SolverException(PERSON_NOT_FOUND));

    principal.recordDeduplicationMatchFound(duplicate.getGlobalId());
    duplicate.markAsMerged(principal.getGlobalId());
    personRepositoryPort.save(duplicate);
    personRepositoryPort.save(principal);

    log.info("event=CORE_WORKFORCE_PERSON_DEDUPLICATION_RESOLVE_SUCCESS targetId={}", principalId);
  }
}

