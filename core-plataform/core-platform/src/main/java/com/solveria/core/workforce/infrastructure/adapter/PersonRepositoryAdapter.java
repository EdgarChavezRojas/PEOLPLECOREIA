package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import com.solveria.core.workforce.infrastructure.mapper.PersonMapper;
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
public class PersonRepositoryAdapter implements PersonRepositoryPort {

  private final PersonRepository personRepository;
  private final PersonMapper personMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public Person save(Person person) {
    PersonJpa personJpa = personMapper.toJpa(person);
    PersonJpa savedPersonJpa = personRepository.save(personJpa);
    Person savedPerson = personMapper.toDomain(savedPersonJpa);

    eventOutboxPort.publish(person.pullDomainEvents());

    return savedPerson;
  }

  @Override
  public Optional<Person> findByGlobalId(String globalId) {
    return personRepository.findByGlobalId(globalId).map(personMapper::toDomain);
  }

  @Override
  public boolean existsByGlobalId(String globalId) {
    return personRepository.existsByGlobalId(globalId);
  }

  @Override
  public Optional<Person> findByPersonId(UUID personId) {
    return personRepository.findById(personId).map(personMapper::toDomain);
  }
}
