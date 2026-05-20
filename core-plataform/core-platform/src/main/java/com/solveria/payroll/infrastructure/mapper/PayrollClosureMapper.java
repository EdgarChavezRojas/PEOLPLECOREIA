package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollClosure;
import com.solveria.payroll.infrastructure.jpa.PayrollClosureJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollClosureMapper {
  @Mapping(target = "id", ignore = true) // Ignores the Long id from BaseEntity
  @Mapping(target = "payrollClosureId", source = "id")
  PayrollClosureJpa toJpa(PayrollClosure domain);

  @Mapping(target = "id", source = "payrollClosureId")
  PayrollClosure toDomain(PayrollClosureJpa jpa);
}
