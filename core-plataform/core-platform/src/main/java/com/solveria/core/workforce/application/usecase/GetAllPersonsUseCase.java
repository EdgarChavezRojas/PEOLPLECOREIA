package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAllPersonsUseCase {
  private final PersonRepositoryPort personRepositoryPort;

  @Transactional(readOnly = true)
  public Page<PersonResponse> execute(Pageable pageable) {
    return personRepositoryPort
        .findAll(pageable)
        .map(
            person ->
                new PersonResponse(
                    person.getPersonId(),
                    person.getFirstName(),
                    person.getLastName(),
                    person.getBirthDate(),
                    person.getGender() != null ? person.getGender().getLabel() : null,
                    person.getGlobalId(),
                    person.getAge(),
                    person.getContactPoint() != null ? person.getContactPoint().getEmail() : null,
                    person.getContactPoint() != null ? person.getContactPoint().getPhone() : null,
                    person.getContactPoint() != null ? person.getContactPoint().getAddress() : null,
                    person.getMaritalStatus() != null ? person.getMaritalStatus().name() : null,
                    person.getProfessionTitle(),
                    person.getCreatedAt(),
                    person.getDNI(),
                    null, // username (no disponible/seguro en listado general)
                    null // tempPassword (solo visible una vez al crear)
                    ));
  }
}
