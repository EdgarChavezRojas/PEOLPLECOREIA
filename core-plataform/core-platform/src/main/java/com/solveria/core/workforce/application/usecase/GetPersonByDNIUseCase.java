package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.dto.GetPersonByDNIResponse;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPersonByDNIUseCase {

  private final PersonRepositoryPort personRepositoryPort;

  @Transactional
  public GetPersonByDNIResponse execute(String DNI) {
    return personRepositoryPort
        .findByCi(DNI)
        .map(
            person ->
                new GetPersonByDNIResponse(
                    person.getGlobalId(),
                    person.getFirstName(),
                    person.getLastName(),
                    person.getDNI(),
                    person.getContactPoint().getEmail(),
                    person.getContactPoint().getPhone()))
        .orElseThrow(() -> new RuntimeException("Person with DNI " + DNI + " not found"));
  }
}
