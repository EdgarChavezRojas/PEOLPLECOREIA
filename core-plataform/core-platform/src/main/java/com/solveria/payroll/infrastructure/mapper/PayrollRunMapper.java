package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.domain.model.entity.PayrollLine;
import com.solveria.payroll.infrastructure.jpa.PayrollLineJpa;
import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollRunMapper {

  PayrollRunJpa toJpa(PayrollRun domain);

  // Nota: Si jpa.getId() devuelve un Long (por el BaseEntity),
  // asegúrate de pasar el campo UUID correcto aquí, por ejemplo: jpa.getRunId() si lo creaste.
  @Mapping(target = "lines", expression = "java(mapLines(jpa.getLines(), jpa.getPayrollRunId()))")
  @Mapping(target = "id", source = "payrollRunId")
  PayrollRun toDomain(PayrollRunJpa jpa);

  default List<PayrollLine> mapLines(List<PayrollLineJpa> lineJpas, UUID payrollRunId) {
    if (lineJpas == null) {
      return null;
    }

    return lineJpas.stream()
        .map(
            lineJpa ->
                new PayrollLine(
                    // Asegúrate de que el primer parámetro sea el UUID de la línea, no el Long
                    // heredado.
                    // Si la BD autogenera UUIDs, usa el campo específico que hayas definido.
                    lineJpa.getLineId(),
                    payrollRunId,
                    lineJpa.getEmployeeId(),
                    lineJpa.getBasicSalary(),
                    lineJpa.getTotalEarned(),
                    lineJpa.getRcIvaRetained(),
                    lineJpa.getGestoraRetained(),
                    lineJpa.getOtherDeductions(),
                    lineJpa.getNetPayable(),
                    lineJpa.getTenantId()))
        .collect(Collectors.toList());
  }
}
