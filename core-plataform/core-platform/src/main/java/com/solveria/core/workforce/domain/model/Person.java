package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.PersonCreatedEvent;
import com.solveria.core.workforce.domain.event.PersonDeduplicationMatchFoundEvent;
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
  private Instant createdAt;
  private LocalDate updatedAt;
  private String DNI;
  private Long userId;
  private static final int MINIMUM_AGE = 18;

  public Person() {}

  public Person(
      UUID personId,
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId,
      ContactPoint contactPoint,
      List<PartyIdentifier> identifiers,
      boolean active,
      String mergedIntoGlobalId,
      Instant createdAt,
      LocalDate updatedAt,
      String DNI) {
    this.personId = personId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.gender = gender;
    this.maritalStatus = maritalStatus;
    this.professionTitle = professionTitle;
    this.globalId = globalId;
    this.contactPoint = contactPoint;
    this.identifiers = identifiers != null ? identifiers : new ArrayList<>();
    this.active = active;
    this.mergedIntoGlobalId = mergedIntoGlobalId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.DNI = DNI;
  }

  public Person(
      UUID personId,
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId,
      ContactPoint contactPoint,
      List<PartyIdentifier> identifiers,
      boolean active,
      String mergedIntoGlobalId,
      Instant createdAt,
      LocalDate updatedAt,
      String DNI,
      Long userId) {
    this.personId = personId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.gender = gender;
    this.maritalStatus = maritalStatus;
    this.professionTitle = professionTitle;
    this.globalId = globalId;
    this.contactPoint = contactPoint;
    this.identifiers = identifiers != null ? identifiers : new ArrayList<>();
    this.active = active;
    this.mergedIntoGlobalId = mergedIntoGlobalId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.DNI = DNI;
    this.userId = userId;
  }

  // Getters y Setters
  public UUID getPersonId() {
    return personId;
  }

  public void setPersonId(UUID personId) {
    this.personId = personId;
  }

  // ... (Todos los demás getters y setters omitidos por brevedad, pero en un entorno real los
  // añadirías de la misma forma) ...
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public MaritalStatus getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(MaritalStatus maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public String getProfessionTitle() {
    return professionTitle;
  }

  public void setProfessionTitle(String professionTitle) {
    this.professionTitle = professionTitle;
  }

  public String getGlobalId() {
    return globalId;
  }

  public void setGlobalId(String globalId) {
    this.globalId = globalId;
  }

  public ContactPoint getContactPoint() {
    return contactPoint;
  }

  public void setContactPoint(ContactPoint contactPoint) {
    this.contactPoint = contactPoint;
  }

  public List<PartyIdentifier> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(List<PartyIdentifier> identifiers) {
    this.identifiers = identifiers != null ? identifiers : new ArrayList<>();
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getMergedIntoGlobalId() {
    return mergedIntoGlobalId;
  }

  public void setMergedIntoGlobalId(String mergedIntoGlobalId) {
    this.mergedIntoGlobalId = mergedIntoGlobalId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDate updatedAt) {
    this.updatedAt = updatedAt;
  }

  public static Person create(
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId,
      ContactPoint contactPoint,
      String DNI) {
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
        new Person(
            UUID.randomUUID(),
            firstName,
            lastName,
            birthDate,
            gender,
            maritalStatus,
            professionTitle,
            globalId,
            contactPoint != null ? contactPoint : new ContactPoint(),
            new ArrayList<>(),
            true,
            null,
            Instant.now(),
            LocalDate.now(),
            DNI    );
    person.registerEvent(
        new PersonCreatedEvent(person.getPersonId(), person.getGlobalId(), Instant.now()));
    return person;
  }

  public void addIdentifier(PartyIdentifier identifier, UUID tenantId) {
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
    registerEvent(new PersonUpdatedEvent(personId, tenantId));
  }

  // Métodos de negocio restantes (updateContactPoint, updateMasterData, etc.) se mantienen
  // iguales...
  public void updateContactPoint(ContactPoint newContactPoint, UUID tenantId) {
    this.contactPoint = newContactPoint;
    this.updatedAt = LocalDate.now();
    registerEvent(new PersonUpdatedEvent(personId, tenantId));
  }

  public void updateMasterData(
      String firstName,
      String lastName,
      LocalDate birthDate,
      Gender gender,
      MaritalStatus maritalStatus,
      String professionTitle,
      String globalId,
      UUID tenantId) {
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
    registerEvent(new PersonUpdatedEvent(personId, tenantId));
  }

  public void recordDeduplicationMatchFound(String matchedGlobalId) {
    registerEvent(new PersonDeduplicationMatchFoundEvent(personId, matchedGlobalId, Instant.now()));
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
  public String getDNI() {
    return DNI;
  }
  public void setDNI(String DNI) {
    this.DNI = DNI;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
