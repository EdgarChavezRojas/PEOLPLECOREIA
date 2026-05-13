package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "prl_payroll_line")
public class PayrollLineJpa extends BaseEntity {
    @Column(name = "line_id", updatable = false, columnDefinition = "UUID")
    private UUID lineId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private PayrollRunJpa payrollRun;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "basic_salary")
    private BigDecimal basicSalary;

    @Column(name = "total_earned")
    private BigDecimal totalEarned;

    @Column(name = "rc_iva_retained")
    private BigDecimal rcIvaRetained;

    @Column(name = "gestora_retained")
    private BigDecimal gestoraRetained;

    @Column(name = "other_deductions")
    private BigDecimal otherDeductions;

    @Column(name = "net_payable")
    private BigDecimal netPayable;

    @Column(name = "tenant_id")
    private String tenantId;
    public UUID getLineId() { return lineId; }
    public void setLineId(UUID lineId) { this.lineId = lineId; }
    public PayrollRunJpa getPayrollRun() { return payrollRun; }
    public void setPayrollRun(PayrollRunJpa payrollRun) { this.payrollRun = payrollRun; }

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }

    public BigDecimal getTotalEarned() { return totalEarned; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }

    public BigDecimal getRcIvaRetained() { return rcIvaRetained; }
    public void setRcIvaRetained(BigDecimal rcIvaRetained) { this.rcIvaRetained = rcIvaRetained; }

    public BigDecimal getGestoraRetained() { return gestoraRetained; }
    public void setGestoraRetained(BigDecimal gestoraRetained) { this.gestoraRetained = gestoraRetained; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
