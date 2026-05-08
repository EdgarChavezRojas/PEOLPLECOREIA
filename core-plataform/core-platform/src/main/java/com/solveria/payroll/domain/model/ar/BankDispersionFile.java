package com.solveria.payroll.domain.model.ar;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.domain.model.vo.DispersionStatus;
import com.solveria.core.shared.exceptions.SolverException;

import java.math.BigDecimal;
import java.util.UUID;

public class BankDispersionFile {
    private UUID id;
    private String tenantId;
    private UUID runRef;
    private UUID bankEntityRef;
    private DispersionStatus status;
    private BigDecimal totalAmount;
    private Integer recordCount;
    private String fileHash;

    public BankDispersionFile() {}

    public void generate(boolean allEmployeesHaveBankAccount) {
        if (!allEmployeesHaveBankAccount) {
            throw new SolverExceptionImpl("MISSING_BANK_ACCOUNTS");
        }
        this.status = DispersionStatus.PROCESSING;
    }

    public void complete(String fileHash) {
        if (this.status != DispersionStatus.PROCESSING) {
            throw new SolverExceptionImpl("INVALID_STATE");
        }
        this.fileHash = fileHash;
        this.status = DispersionStatus.COMPLETED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getRunRef() {
        return runRef;
    }

    public void setRunRef(UUID runRef) {
        this.runRef = runRef;
    }

    public UUID getBankEntityRef() {
        return bankEntityRef;
    }

    public void setBankEntityRef(UUID bankEntityRef) {
        this.bankEntityRef = bankEntityRef;
    }

    public DispersionStatus getStatus() {
        return status;
    }

    public void setStatus(DispersionStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
}
