package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.infrastructure.jpa.PartyIdentifierJpa;
import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {PartyIdentifierMapper.class})
public interface PersonMapper {



  PersonJpa toJpa(Person person);


  Person toDomain(PersonJpa jpa);

  @Mapping(
      target = "gender",
      expression = "java(person.getGender() != null ? person.getGender().name() : null)")
  @Mapping(
      target = "maritalStatus",
      expression =
          "java(person.getMaritalStatus() != null ? person.getMaritalStatus().name() : null)")
  @Mapping(target = "professionTitle", source = "professionTitle")
  @Mapping(target = "email", source = "contactPoint.email")
  @Mapping(target = "phone", source = "contactPoint.phone")
  @Mapping(target = "address", source = "contactPoint.address")
  PersonResponse toResponse(Person person);

  @AfterMapping
  default void setBackReference(@MappingTarget PersonJpa personJpa) {
    if (personJpa.getIdentifiers() == null) {
      return;
    }
    for (PartyIdentifierJpa identifier : personJpa.getIdentifiers()) {
      identifier.setPerson(personJpa);
    }
  }

  default String toEventPayload(Person person) {
    if (person == null) return "{}";

    try {
      return new com.fasterxml.jackson.databind.ObjectMapper()
          .writeValueAsString(
              java.util.Map.ofEntries(
                  java.util.Map.entry("personId", person.getPersonId()),
                  java.util.Map.entry("firstName", person.getFirstName()),
                  java.util.Map.entry("lastName", person.getLastName()),
                  java.util.Map.entry("birthDate", person.getBirthDate().toString()),
                  java.util.Map.entry("gender", person.getGender().name()),
                  java.util.Map.entry("globalId", person.getGlobalId()),
                  java.util.Map.entry("age", person.getAge())));
    } catch (Exception e) {
      throw new RuntimeException("Error serializando Person a JSON", e);
    }
  }
}
