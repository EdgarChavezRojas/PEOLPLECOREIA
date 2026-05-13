package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollPeriod;
import com.solveria.payroll.infrastructure.jpa.PayrollPeriodJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollPeriodMapper {
    @Mapping(target = "cutoffDate", source = "cutoffDate.date")
    PayrollPeriodJpa toJpa(PayrollPeriod domain);

    @Mapping(target = "cutoffDate", expression = "java(new com.solveria.payroll.domain.model.vo.CutoffDate(jpa.getCutoffDate()))")
    PayrollPeriod toDomain(PayrollPeriodJpa jpa);
}
