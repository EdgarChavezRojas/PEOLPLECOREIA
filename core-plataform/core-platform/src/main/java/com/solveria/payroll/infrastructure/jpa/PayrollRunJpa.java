package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prl_payroll_run")
public class PayrollRunJpa extends BaseEntity {

    @Column(name = "period_ref")
    private UUID periodRef;

    @Column(name = "group_ref")
    private UUID groupRef;

    @Column(name = "run_type")
    private String runType;

    @Column(name = "status")
    private String status;

    @Column(name = "tenant_id")
    private String tenantId;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollLineJpa> lines = new ArrayList<>();

    public UUID getPeriodRef() { return periodRef; }
    public void setPeriodRef(UUID periodRef) { this.periodRef = periodRef; }

    public UUID getGroupRef() { return groupRef; }
    public void setGroupRef(UUID groupRef) { this.groupRef = groupRef; }

    public String getRunType() { return runType; }
    public void setRunType(String runType) { this.runType = runType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public List<PayrollLineJpa> getLines() { return lines; }
    public void setLines(List<PayrollLineJpa> lines) { this.lines = lines; }
    
    public void addLine(PayrollLineJpa line) {
        lines.add(line);
        line.setPayrollRun(this);
    }
}
