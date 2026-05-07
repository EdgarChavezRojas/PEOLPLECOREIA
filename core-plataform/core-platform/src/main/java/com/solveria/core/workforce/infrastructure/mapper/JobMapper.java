package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.Job;
import com.solveria.core.workforce.infrastructure.jpa.JobJpa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobMapper {

  JobJpa toJpa(Job job);

  Job toDomain(JobJpa jpa);
}
