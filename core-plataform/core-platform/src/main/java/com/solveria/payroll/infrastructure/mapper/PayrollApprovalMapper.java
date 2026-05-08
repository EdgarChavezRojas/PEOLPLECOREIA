package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollApprovalMapper {
    PayrollApprovalJpa toJpa(PayrollApproval domain);
    PayrollApproval toDomain(PayrollApprovalJpa jpa);
}
