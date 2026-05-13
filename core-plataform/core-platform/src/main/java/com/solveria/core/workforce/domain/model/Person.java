package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.PersonCreatedEvent;
import com.solveria.core.workforce.domain.event.PersonDeduplicationMatchFoundEvent;
import com.solveria.core.workforce.domain.event.PersonMasterCreatedEvent;
import com.solveria.core.workforce.domain.event.PersonUpdatedEvent;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.Gender;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person extends DomainRoot {

  private UUID personId;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private Gender gender;
  private MaritalStatus maritalStatus;
  private String professionTitle;
  private String globalId;
  private ContactPoint contactPoint;
  private List<PartyIdentifier> identifiers;
  private boolean active;
  private String mergedIntoGlobalId;
  private LocalDate createdAt;
  private LocalDate updatedAt;



  private static final int MINIMUM_AGE = 18;

  public static Person create(
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId,
      ContactPoint contactPoint) {
    Period age = Period.between(birthDate, LocalDate.now());
    if (age.getYears() < MINIMUM_AGE) {
      throw new IllegalArgumentException("La persona debe tener minimo " + MINIMUM_AGE + " anos");
    }

    if (firstName == null
        || firstName.isBlank()
        || lastName == null
        || lastName.isBlank()
        || globalId == null
        || globalId.isBlank()) {
      throw new IllegalArgumentException("firstName, lastName y globalId son requeridos");
    }

    Person person =
        Person.builder()
            .personId(UUID.randomUUID())
            .firstName(firstName)
            .lastName(lastName)
            .birthDate(birthDate)
            .gender(gender)
            .maritalStatus(maritalStatus)
            .professionTitle(professionTitle)
            .globalId(globalId)
            .contactPoint(contactPoint != null ? contactPoint : new ContactPoint())
            .identifiers(new ArrayList<>())
            .active(true)
            .mergedIntoGlobalId(null)
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .build();
    person.registerEvent(new PersonCreatedEvent(person.personId, person.globalId, Instant.now()));
    return person;
  }

  public void addIdentifier(PartyIdentifier identifier) {
    if (identifier == null) {
      throw new IllegalArgumentException("identifier no puede ser nulo");
    }
    boolean exists =
        identifiers.stream().anyMatch(id -> id.getIdNumber().equals(identifier.getIdNumber()));
    if (exists) {
      throw new IllegalArgumentException(
          "El identificador " + identifier.getIdNumber() + " ya esta vinculado a esta persona");
    }
    identifiers.add(identifier);
    this.updatedAt = LocalDate.now();
    registerEvent(new PersonUpdatedEvent(personId, Instant.now()));
  }

  public void updateContactPoint(ContactPoint newContactPoint) {
    this.contactPoint = newContactPoint;
    this.updatedAt = LocalDate.now();
    registerEvent(new PersonUpdatedEvent(personId, Instant.now()));
  }

  public void updateMasterData(
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId) {
    if (firstName == null
        || firstName.isBlank()
        || lastName == null
        || lastName.isBlank()
        || globalId == null
        || globalId.isBlank()) {
      throw new IllegalArgumentException("firstName, lastName y globalId son requeridos");
    }
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.gender = gender;
    this.maritalStatus = maritalStatus;
    this.professionTitle = professionTitle;
    this.globalId = globalId;
    this.updatedAt = LocalDate.now();
    registerEvent(new PersonUpdatedEvent(personId, Instant.now()));
  }

  public void markAsMasterCreated() {
    registerEvent(new PersonMasterCreatedEvent(personId, Instant.now()));
  }

  public void recordDeduplicationMatchFound(String matchedGlobalId) {
    registerEvent(
        new PersonDeduplicationMatchFoundEvent(personId, matchedGlobalId, Instant.now()));
  }

  public void markAsMerged(String principalGlobalId) {
    if (principalGlobalId == null || principalGlobalId.isBlank()) {
      throw new IllegalArgumentException("principalGlobalId es requerido");
    }
    this.active = false;
    this.mergedIntoGlobalId = principalGlobalId;
    this.updatedAt = LocalDate.now();
  }

  public Integer getAge() {
    return Period.between(birthDate, LocalDate.now()).getYears();
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }


}
