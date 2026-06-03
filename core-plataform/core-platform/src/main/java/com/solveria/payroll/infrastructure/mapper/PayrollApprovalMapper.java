package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollApprovalMapper {

  @Mapping(target = "payrollApprovalId", source = "id")
  PayrollApprovalJpa toJpa(PayrollApproval domain);

  @Mapping(target = "id", source = "payrollApprovalId")
  PayrollApproval toDomain(PayrollApprovalJpa jpa);

  void updateJpa(PayrollApproval domain, @MappingTarget PayrollApprovalJpa jpa);
}
