package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.BankEntity;
import com.solveria.payroll.infrastructure.jpa.BankEntityJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankEntityMapper {

  @Mapping(target = "bankCode", source = "bankCode.code")
  @Mapping(target = "fileFormat", source = "fileFormatSpec")
  BankEntityJpa toJpa(BankEntity domain);

  @Mapping(
      target = "bankCode",
      expression = "java(new com.solveria.payroll.domain.model.vo.BankCode(jpa.getBankCode()))")
  @Mapping(target = "fileFormatSpec", source = "fileFormat")
  BankEntity toDomain(BankEntityJpa jpa);
}
