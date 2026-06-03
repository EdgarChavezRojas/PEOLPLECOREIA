package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.PersonLookupPO;
import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import com.solveria.core.workforce.infrastructure.repository.PersonRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonLookupAdapter implements PersonLookupPO {

  private final PersonRepository personRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<UUID> findPersonIdByUserId(Long userId) {
    if (userId == null) {
      return Optional.empty();
    }
    return personRepository.findByUserId(userId).map(PersonJpa::getPersonId);
  }
}
