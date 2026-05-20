package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_bank_entity} — Persistencia de la Entidad Bancaria.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at}, {@code
 * created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 */
@Entity
@Table(name = "prl_bank_entity")
public class BankEntityJpa extends BaseEntity {

  @Column(
      name = "bank_entity_id",
      nullable = false,
      unique = true,
      updatable = false,
      columnDefinition = "UUID")
  private UUID bankEntityId;

  @Column(name = "bank_code", nullable = false, length = 30)
  private String bankCode;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "file_format", length = 100)
  private String fileFormat;

  public BankEntityJpa() {
    // JPA
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getBankEntityId() {
    return bankEntityId;
  }

  public void setBankEntityId(UUID bankEntityId) {
    this.bankEntityId = bankEntityId;
  }

  public String getBankCode() {
    return bankCode;
  }

  public void setBankCode(String bankCode) {
    this.bankCode = bankCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
  }
}
