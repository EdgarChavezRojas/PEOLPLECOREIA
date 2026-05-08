package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollRunMapper {

    PayrollRunJpa toJpa(PayrollRun domain);

    @Mapping(target = "lines", expression = "java(mapLines(jpa.getLines(), jpa.getId()))")
    PayrollRun toDomain(PayrollRunJpa jpa);

    default java.util.List<com.solveria.payroll.domain.model.entity.PayrollLine> mapLines(java.util.List<com.solveria.core.payroll.infrastructure.jpa.PayrollLineJpa> lines, java.util.UUID runRef) {
        if (lines == null) return null;
        return lines.stream().map(lineJpa -> new com.solveria.core.payroll.domain.model.entity.PayrollLine(
                lineJpa.getId(), runRef, lineJpa.getEmployeeId(), lineJpa.getBasicSalary(),
                lineJpa.getTotalEarned(), lineJpa.getRcIvaRetained(), lineJpa.getGestoraRetained(),
                lineJpa.getOtherDeductions(), lineJpa.getNetPayable(), lineJpa.getTenantId()
        )).collect(java.util.stream.Collectors.toList());
    }
}
