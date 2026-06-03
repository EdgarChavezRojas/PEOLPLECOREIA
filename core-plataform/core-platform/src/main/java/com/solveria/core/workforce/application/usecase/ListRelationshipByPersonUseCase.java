package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.Relationship;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRelationshipByPersonUseCase {

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  private final PersonRepositoryPort personRepositoryPort;

  public List<Relationship> execute(Long userId) {
    Person person = personRepositoryPort.findByUserId(userId).orElseThrow();
    return relationshipRepositoryPort.findByPersonId(person.getPersonId());
  }
}
