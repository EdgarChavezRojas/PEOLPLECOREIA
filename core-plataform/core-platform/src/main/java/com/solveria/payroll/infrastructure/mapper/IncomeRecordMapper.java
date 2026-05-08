package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.IncomeRecord;
import com.solveria.payroll.infrastructure.jpa.IncomeRecordJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IncomeRecordMapper {

    @Mapping(target = "amount", source = "amount.value")
    @Mapping(target = "isAutomatic", source = "automatic")
    IncomeRecordJpa toJpa(IncomeRecord domain);

    @Mapping(target = "amount", expression = "java(new com.solveria.core.payroll.domain.model.vo.IncomeAmount(jpa.getAmount()))")
    @Mapping(target = "isAutomatic", source = "isAutomatic")
    IncomeRecord toDomain(IncomeRecordJpa jpa);
}
