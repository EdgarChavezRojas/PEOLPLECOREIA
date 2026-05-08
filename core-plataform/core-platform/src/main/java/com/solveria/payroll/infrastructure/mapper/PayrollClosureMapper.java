package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollClosure;
import com.solveria.payroll.infrastructure.jpa.PayrollClosureJpa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollClosureMapper {
    PayrollClosureJpa toJpa(PayrollClosure domain);
    PayrollClosure toDomain(PayrollClosureJpa jpa);
}
