package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.AcademicProfile;
import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import com.solveria.core.workforce.infrastructure.jpa.AcademicProfileJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AcademicProfileMapper {

  @Mapping(target = "relationship", ignore = true)
  @Mapping(target = "currentRank", source = "currentRank", qualifiedByName = "toRank")
  AcademicProfileJpa toJpa(AcademicProfile academicProfile);

  @Mapping(target = "relationshipId", source = "relationship.relationshipId")
  @Mapping(target = "currentRank", source = "currentRank", qualifiedByName = "toRankEnum")
  AcademicProfile toDomain(AcademicProfileJpa jpa);

  @Named("toRank")
  default String toRank(AcademicRank rank) {
    return rank != null ? rank.name() : null;
  }

  @Named("toRankEnum")
  default AcademicRank toRankEnum(String value) {
    return value != null ? AcademicRank.valueOf(value) : null;
  }
}
