package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.model.PartyIdentifier;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.vo.Extension;
import com.solveria.core.workforce.infrastructure.jpa.PartyIdentifierJpa;
import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import com.solveria.core.workforce.infrastructure.mapper.PartyIdentifierMapper;
import com.solveria.core.workforce.infrastructure.mapper.PersonMapper;
import com.solveria.core.workforce.infrastructure.repository.PersonRepository;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonRepositoryAdapter implements PersonRepositoryPort {

  private final PersonRepository personRepository;
  private final PersonMapper personMapper;
  private final PartyIdentifierMapper partyIdentifierMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "persons", key = "#result.personId", condition = "#result != null"),
        @CacheEvict(value = "personsByCi", key = "#result.DNI", condition = "#result != null"),
        @CacheEvict(
            value = "personsByGlobalId",
            key = "#result.globalId",
            condition = "#result != null"),
        @CacheEvict(
            value = "personsByUserId",
            key = "#result.userId",
            condition = "#result != null")
      })
  public Person save(Person person) {
    PersonJpa personJpa =
        personRepository
            .findById(person.getPersonId())
            .map(
                existing -> {
                  personMapper.updateJpa(person, existing);
                  mergeIdentifiers(person, existing);
                  return existing;
                })
            .orElseGet(() -> personMapper.toJpa(person));

    PersonJpa savedPersonJpa = personRepository.save(personJpa);
    Person savedPerson = personMapper.toDomain(savedPersonJpa);

    eventOutboxPort.publish(person.pullDomainEvents());

    return savedPerson;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "personsByGlobalId", key = "#globalId", unless = "#result == null")
  public Optional<Person> findByGlobalId(String globalId) {
    return personRepository.findByGlobalId(globalId).map(personMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByGlobalId(String globalId) {
    return personRepository.existsByGlobalId(globalId);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "persons", key = "#personId", unless = "#result == null")
  public Optional<Person> findByPersonId(UUID personId) {
    return personRepository.findById(personId).map(personMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "personsByCi", key = "#DNI", unless = "#result == null")
  public Optional<Person> findByCi(String DNI) {
    return personRepository.findByDNI(DNI).map(personMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Person> findAll(Pageable pageable) {
    return personRepository.findAll(pageable).map(personMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "personsByUserId", key = "#userId", unless = "#result == null")
  public Optional<Person> findByUserId(Long userId) {
    return personRepository.findByUserId(userId).map(personMapper::toDomain);
  }

  private void mergeIdentifiers(Person person, PersonJpa existing) {
    if (person.getIdentifiers() == null || person.getIdentifiers().isEmpty()) {
      return;
    }
    if (existing.getIdentifiers() == null) {
      existing.setIdentifiers(new ArrayList<>());
    }
    Map<UUID, PartyIdentifierJpa> existingById =
        existing.getIdentifiers().stream()
            .filter(id -> id.getIdentifierId() != null)
            .collect(Collectors.toMap(PartyIdentifierJpa::getIdentifierId, Function.identity()));

    for (PartyIdentifier identifier : person.getIdentifiers()) {
      if (identifier == null || identifier.getIdentifierId() == null) {
        continue;
      }
      PartyIdentifierJpa target = existingById.get(identifier.getIdentifierId());
      if (target == null) {
        PartyIdentifierJpa created = partyIdentifierMapper.toJpa(identifier);
        created.setPerson(existing);
        created.setTenantId(existing.getTenantId());
        existing.getIdentifiers().add(created);
      } else {
        target.setIdType(identifier.getIdType() != null ? identifier.getIdType().name() : null);
        target.setIdNumber(identifier.getIdNumber());
        target.setExtension(
            identifier.getExtension() != null
                ? (identifier.getExtension() == Extension.SCZ
                    ? "SC"
                    : identifier.getExtension().name())
                : null);
        target.setIssueDate(identifier.getIssueDate());
        target.setExpiryDate(identifier.getExpiryDate());
        target.setPerson(existing);
      }
    }

    Set<UUID> domainIds =
        person.getIdentifiers().stream()
            .map(PartyIdentifier::getIdentifierId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    existing.getIdentifiers().removeIf(id -> !domainIds.contains(id.getIdentifierId()));
  }
}
