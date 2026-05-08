package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "prl_payroll_closure")
public class PayrollClosureJpa extends BaseEntity {

    @Column(name = "run_ref", nullable = false)
    private UUID runRef;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "integrity_hash")
    private String integrityHash;

    public UUID getRunRef() { return runRef; }
    public void setRunRef(UUID runRef) { this.runRef = runRef; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIntegrityHash() { return integrityHash; }
    public void setIntegrityHash(String integrityHash) { this.integrityHash = integrityHash; }
}
