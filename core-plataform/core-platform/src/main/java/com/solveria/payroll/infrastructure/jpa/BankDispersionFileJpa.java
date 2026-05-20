package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "prl_dispersion_file")
public class BankDispersionFileJpa extends BaseEntity {

  @Column(name = "dispersion_file_id", updatable = false, columnDefinition = "UUID")
  private UUID dispersionFileId;

  // Add getters and setters
  @Column(name = "run_ref")
  private UUID runRef;

  @Column(name = "bank_entity_ref")
  private UUID bankEntityRef;

  @Column(name = "status")
  private String status;

  @Column(name = "total_amount")
  private BigDecimal totalAmount;

  @Column(name = "record_count")
  private Integer recordCount;

  @Column(name = "file_hash")
  private String fileHash;

  @Column(name = "tenant_id")
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public UUID getDispersionFileId() {
    return dispersionFileId;
  }

  public void setDispersionFileId(UUID dispersionFileId) {
    this.dispersionFileId = dispersionFileId;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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
