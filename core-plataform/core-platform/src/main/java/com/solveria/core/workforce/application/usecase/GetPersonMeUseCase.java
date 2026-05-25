package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.dto.PersonMeResponse;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPersonMeUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    @Transactional(readOnly = true)
    public PersonMeResponse execute(Long userId) {
        Person person = personRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new PersonNotFoundException(
                        "Person not found for userId: " + userId));

        return new PersonMeResponse(
                person.getPersonId(),
                person.getFirstName(),
                person.getLastName(),
                person.getContactPoint() != null ? person.getContactPoint().getEmail() : null,
                person.getDNI()
        );
    }
}
