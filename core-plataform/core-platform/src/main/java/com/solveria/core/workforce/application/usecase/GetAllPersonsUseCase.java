package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllPersonsUseCase {
    private final PersonRepositoryPort personRepositoryPort;

    public Page<PersonResponse> execute(Pageable pageable) {
        return personRepositoryPort.findAll(pageable)
                .map(person -> new PersonResponse(
                        person.getPersonId(),
                        person.getFirstName(),
                        person.getLastName(),
                        person.getBirthDate(),
                        person.getGender().getLabel(),
                        person.getGlobalId(),
                        person.getAge(),
                        person.getContactPoint().getEmail(),
                        person.getContactPoint().getPhone(),
                        person.getContactPoint().getAddress(),
                        person.getMaritalStatus().name(),
                        person.getProfessionTitle(),
                        person.getCreatedAt(),
                        person.getDNI()
                ));
    }

}