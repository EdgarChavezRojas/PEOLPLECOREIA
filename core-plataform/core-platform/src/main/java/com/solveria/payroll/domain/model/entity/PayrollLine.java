package com.solveria.payroll.domain.model.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class PayrollLine {
  private final UUID id;
  private final UUID runId;
  private final UUID employeeId;
  private BigDecimal basicSalary;
  private BigDecimal totalEarned;
  private BigDecimal rcIvaRetained;
  private BigDecimal gestoraRetained;
  private BigDecimal otherDeductions;
  private BigDecimal netPayable;
  private final UUID tenantId;
  private BigDecimal seniorityBonus;
  private BigDecimal infocalRetained;
  private BigDecimal fiscalCredit;

  public PayrollLine(
      UUID id,
      UUID runId,
      UUID employeeId,
      BigDecimal basicSalary,
      BigDecimal totalEarned,
      BigDecimal rcIvaRetained,
      BigDecimal gestoraRetained,
      BigDecimal otherDeductions,
      BigDecimal netPayable,
      UUID tenantId) {
    this(
        id,
        runId,
        employeeId,
        basicSalary,
        totalEarned,
        rcIvaRetained,
        gestoraRetained,
        otherDeductions,
        netPayable,
        tenantId,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO);
  }

  public PayrollLine(
      UUID id,
      UUID runId,
      UUID employeeId,
      BigDecimal basicSalary,
      BigDecimal totalEarned,
      BigDecimal rcIvaRetained,
      BigDecimal gestoraRetained,
      BigDecimal otherDeductions,
      BigDecimal netPayable,
      UUID tenantId,
      BigDecimal seniorityBonus,
      BigDecimal infocalRetained,
      BigDecimal fiscalCredit) {
    this.id = id;
    this.runId = runId;
    this.employeeId = employeeId;
    this.basicSalary = basicSalary;
    this.totalEarned = totalEarned;
    this.rcIvaRetained = rcIvaRetained;
    this.gestoraRetained = gestoraRetained;
    this.otherDeductions = otherDeductions;
    this.netPayable = netPayable;
    this.tenantId = tenantId;
    this.seniorityBonus = seniorityBonus != null ? seniorityBonus : BigDecimal.ZERO;
    this.infocalRetained = infocalRetained != null ? infocalRetained : BigDecimal.ZERO;
    this.fiscalCredit = fiscalCredit != null ? fiscalCredit : BigDecimal.ZERO;
  }

  public void recalculateNet() {
    this.netPayable =
        totalEarned
            .subtract(rcIvaRetained)
            .subtract(gestoraRetained)
            .subtract(infocalRetained)
            .subtract(otherDeductions);
  }

  public UUID getId() {
    return id;
  }

  public UUID getRunId() {
    return runId;
  }

  public UUID getEmployeeId() {
    return employeeId;
  }

  public BigDecimal getBasicSalary() {
    return basicSalary;
  }

  public BigDecimal getTotalEarned() {
    return totalEarned;
  }

  public BigDecimal getRcIvaRetained() {
    return rcIvaRetained;
  }

  public BigDecimal getGestoraRetained() {
    return gestoraRetained;
  }

  public BigDecimal getOtherDeductions() {
    return otherDeductions;
  }

  public BigDecimal getNetPayable() {
    return netPayable;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public BigDecimal getSeniorityBonus() {
    return seniorityBonus;
  }

  public void setSeniorityBonus(BigDecimal seniorityBonus) {
    this.seniorityBonus = seniorityBonus;
  }

  public BigDecimal getInfocalRetained() {
    return infocalRetained;
  }

  public void setInfocalRetained(BigDecimal infocalRetained) {
    this.infocalRetained = infocalRetained;
  }

  public BigDecimal getFiscalCredit() {
    return fiscalCredit;
  }

  public void setFiscalCredit(BigDecimal fiscalCredit) {
    this.fiscalCredit = fiscalCredit;
  }

  public void setBasicSalary(BigDecimal basicSalary) {
    this.basicSalary = basicSalary;
  }

  public void setTotalEarned(BigDecimal totalEarned) {
    this.totalEarned = totalEarned;
  }

  public void setRcIvaRetained(BigDecimal rcIvaRetained) {
    this.rcIvaRetained = rcIvaRetained;
  }

  public void setGestoraRetained(BigDecimal gestoraRetained) {
    this.gestoraRetained = gestoraRetained;
  }

  public void setOtherDeductions(BigDecimal otherDeductions) {
    this.otherDeductions = otherDeductions;
  }

  public void setNetPayable(BigDecimal netPayable) {
    this.netPayable = netPayable;
  }
}
