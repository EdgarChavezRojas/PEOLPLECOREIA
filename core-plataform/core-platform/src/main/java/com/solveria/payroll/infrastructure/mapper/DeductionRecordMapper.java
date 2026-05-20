package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.infrastructure.jpa.DeductionRecordJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeductionRecordMapper {

  @Mapping(target = "amount", source = "amount.value")
  @Mapping(target = "isAutomatic", source = "automatic")
  DeductionRecordJpa toJpa(DeductionRecord domain);

  @Mapping(
      target = "amount",
      expression =
          "java(new com.solveria.payroll.domain.model.vo.DeductionAmount(jpa.getAmount()))")
  @Mapping(target = "isAutomatic", source = "isAutomatic")
  DeductionRecord toDomain(DeductionRecordJpa jpa);
}
