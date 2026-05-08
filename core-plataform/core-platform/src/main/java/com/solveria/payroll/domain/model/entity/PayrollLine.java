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
    private final String tenantId;

    public PayrollLine(UUID id, UUID runId, UUID employeeId, BigDecimal basicSalary, 
                       BigDecimal totalEarned, BigDecimal rcIvaRetained, 
                       BigDecimal gestoraRetained, BigDecimal otherDeductions, 
                       BigDecimal netPayable, String tenantId) {
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
    }

    public void recalculateNet() {
        this.netPayable = totalEarned.subtract(rcIvaRetained)
                                     .subtract(gestoraRetained)
                                     .subtract(otherDeductions);
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public UUID getEmployeeId() { return employeeId; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public BigDecimal getRcIvaRetained() { return rcIvaRetained; }
    public BigDecimal getGestoraRetained() { return gestoraRetained; }
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public BigDecimal getNetPayable() { return netPayable; }
    public String getTenantId() { return tenantId; }

    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }
    public void setRcIvaRetained(BigDecimal rcIvaRetained) { this.rcIvaRetained = rcIvaRetained; }
    public void setGestoraRetained(BigDecimal gestoraRetained) { this.gestoraRetained = gestoraRetained; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }
}
