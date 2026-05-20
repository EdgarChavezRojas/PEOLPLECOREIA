package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollGroup;
import com.solveria.payroll.infrastructure.jpa.PayrollGroupJpa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollGroupMapper {
  PayrollGroupJpa toJpa(PayrollGroup domain);

  PayrollGroup toDomain(PayrollGroupJpa jpa);
}
