package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.application.dto.CreatePersonRequest;
import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.domain.exception.PersonAlreadyExistsException;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.Gender;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import com.solveria.core.workforce.infrastructure.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePersonUseCase {

  private final PersonRepositoryPort personRepositoryPort;
  private final PersonMapper personMapper;

  @Transactional
  public PersonResponse execute(CreatePersonRequest request) {
    if (personRepositoryPort.existsByGlobalId(request.getGlobalId())) {
      throw new PersonAlreadyExistsException("PersonID ya existe: " + request.getGlobalId());
    }

    ContactPoint contact =
        ContactPoint.create(request.getEmail(), request.getPhone(), request.getAddress());

    Gender gender = Gender.valueOf(request.getGender().toUpperCase());
    MaritalStatus maritalStatus = request.getMaritalStatus() != null ?
            MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()) : null;
    Person person =
        Person.create(
            request.getFirstName(),
            request.getLastName(),
            request.getBirthDate(),
            gender,
            maritalStatus,
            request.getProfessionTitle(),
            request.getGlobalId(),
            contact);

    Person savedPerson = personRepositoryPort.save(person);

    log.info(
        "event=PERSON_CREATE_SUCCESS personId={} firstName={}",
        savedPerson.getPersonId(),
        savedPerson.getFirstName());

    return personMapper.toResponse(savedPerson);
  }
}
