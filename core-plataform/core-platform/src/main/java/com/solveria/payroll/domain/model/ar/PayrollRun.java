package com.solveria.payroll.domain.model.ar;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.domain.model.entity.PayrollLine;
import com.solveria.payroll.domain.model.vo.PayrollRunType;
import com.solveria.payroll.domain.model.vo.PayrollStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PayrollRun {
  private final UUID id;
  private final UUID periodRef;
  private final UUID groupRef;
  private final PayrollRunType runType;
  private PayrollStatus status;
  private final UUID tenantId;
  private final List<PayrollLine> lines;

  public PayrollRun(
      UUID id,
      UUID periodRef,
      UUID groupRef,
      PayrollRunType runType,
      PayrollStatus status,
      UUID tenantId,
      List<PayrollLine> lines) {
    this.id = id != null ? id : UUID.randomUUID();
    this.periodRef = periodRef;
    this.groupRef = groupRef;
    this.runType = runType;
    this.status = status;
    this.tenantId = tenantId;
    this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
  }

  public void addLine(PayrollLine line) {
    this.lines.add(line);
  }

  public void generateDraft() {
    for (PayrollLine line : lines) {
      line.recalculateNet();
      if (line.getNetPayable().compareTo(BigDecimal.ZERO) < 0) {
        throw new SolverExceptionImpl(
            "Líquido pagable negativo detectado para el empleado: " + line.getEmployeeId());
      }
    }
    this.status = PayrollStatus.BORRADOR;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPeriodRef() {
    return periodRef;
  }

  public UUID getGroupRef() {
    return groupRef;
  }

  public PayrollRunType getRunType() {
    return runType;
  }

  public PayrollStatus getStatus() {
    return status;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public List<PayrollLine> getLines() {
    return Collections.unmodifiableList(lines);
  }
}
