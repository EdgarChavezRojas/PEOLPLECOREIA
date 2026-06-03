package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.workforce.domain.model.vo.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

  @Override
  public String convertToDatabaseColumn(Gender attribute) {
    if (attribute == null) {
      return null;
    }
    return switch (attribute) {
      case MALE -> "M";
      case FEMALE -> "F";
      case OTHER -> "OTHER";
    };
  }

  @Override
  public Gender convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return switch (dbData.toUpperCase()) {
      case "M", "MALE" -> Gender.MALE;
      case "F", "FEMALE" -> Gender.FEMALE;
      case "OTHER" -> Gender.OTHER;
      default -> throw new IllegalArgumentException("Unknown gender: " + dbData);
    };
  }
}
