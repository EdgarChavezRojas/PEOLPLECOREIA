package com.solveria.payroll.application.mapper;

import com.solveria.payroll.application.dto.response.DispersionFileResponse;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollApiMapper {

    @Mapping(source = "periodRef", target = "periodId")
    PayrollRunResponse toResponse(PayrollRun payrollRun);

    @Mapping(source = "runRef", target = "payrollRunId")
    @Mapping(source = "bankEntityRef", target = "bankCode")
    DispersionFileResponse toResponse(BankDispersionFile bankDispersionFile);
}
