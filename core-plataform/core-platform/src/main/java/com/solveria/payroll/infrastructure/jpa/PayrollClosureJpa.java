package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prl_payroll_closure")
public class PayrollClosureJpa extends BaseEntity {
  @Id
  @Column(name = "payroll_closure_id", updatable = false, columnDefinition = "UUID")
  private UUID payrollClosureId;

  @Column(name = "run_ref", nullable = false)
  private UUID runRef;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "integrity_hash")
  private String integrityHash;

  @Column(name = "tenant_id")
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public UUID getPayrollClosureId() {
    return payrollClosureId;
  }

  public void setPayrollClosureId(UUID payrollClosureId) {
    this.payrollClosureId = payrollClosureId;
  }

  public UUID getRunRef() {
    return runRef;
  }

  public void setRunRef(UUID runRef) {
    this.runRef = runRef;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getIntegrityHash() {
    return integrityHash;
  }

  public void setIntegrityHash(String integrityHash) {
    this.integrityHash = integrityHash;
  }
}
