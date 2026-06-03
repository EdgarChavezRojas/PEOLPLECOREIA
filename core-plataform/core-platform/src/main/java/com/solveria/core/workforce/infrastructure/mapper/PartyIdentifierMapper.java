package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.PartyIdentifier;
import com.solveria.core.workforce.domain.model.vo.Extension;
import com.solveria.core.workforce.domain.model.vo.PartyIdentifierType;
import com.solveria.core.workforce.infrastructure.jpa.PartyIdentifierJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PartyIdentifierMapper {

  @Mapping(target = "person", ignore = true)
  @Mapping(target = "idType", source = "idType", qualifiedByName = "toIdType")
  @Mapping(target = "extension", source = "extension", qualifiedByName = "toExtension")
  PartyIdentifierJpa toJpa(PartyIdentifier partyIdentifier);

  @Mapping(target = "personId", source = "person.personId")
  @Mapping(target = "idType", source = "idType", qualifiedByName = "toIdTypeEnum")
  @Mapping(target = "extension", source = "extension", qualifiedByName = "toExtensionEnum")
  PartyIdentifier toDomain(PartyIdentifierJpa jpa);

  @Named("toIdType")
  default String toIdType(PartyIdentifierType type) {
    return type != null ? type.name() : null;
  }

  @Named("toIdTypeEnum")
  default PartyIdentifierType toIdTypeEnum(String value) {
    return value != null ? PartyIdentifierType.valueOf(value) : null;
  }

  @Named("toExtension")
  default String toExtension(Extension extension) {
    if (extension == null) {
      return null;
    }
    if (extension == Extension.SCZ) {
      return "SC";
    }
    return extension.name();
  }

  @Named("toExtensionEnum")
  default Extension toExtensionEnum(String value) {
    if (value == null) {
      return null;
    }
    if (value.equalsIgnoreCase("SC") || value.equalsIgnoreCase("SCZ")) {
      return Extension.SCZ;
    }
    return Extension.valueOf(value.toUpperCase());
  }
  // test
}
