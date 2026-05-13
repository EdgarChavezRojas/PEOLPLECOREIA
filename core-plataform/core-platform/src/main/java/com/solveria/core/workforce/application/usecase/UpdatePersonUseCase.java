package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdatePersonUseCase {

  private static final String PERSON_NOT_FOUND = "PERSON_NOT_FOUND";

  private final PersonRepositoryPort personRepositoryPort;

  public void execute(
      UUID personId,
      MaritalStatus maritalStatus,
      String professionTitle,
      List<ContactPoint> contacts) {
    Person person =
        personRepositoryPort
            .findByPersonId(personId)
            .orElseThrow(() -> new SolverException(PERSON_NOT_FOUND));

    person.updateMasterData(
        person.getFirstName(),
        person.getLastName(),
        person.getBirthDate(),
        person.getGender(),
        maritalStatus,
        professionTitle,
        person.getGlobalId());

    if (contacts != null && !contacts.isEmpty() && contacts.get(0) != null) {
      person.updateContactPoint(contacts.get(0));
    }

    personRepositoryPort.save(person);

    log.info("event=CORE_WORKFORCE_PERSON_UPDATE_SUCCESS targetId={}", personId);
  }
}

