package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_payroll_group} — Persistencia del Grupo de Nómina.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at}, {@code
 * created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 */
@Entity
@Table(name = "prl_payroll_group")
public class PayrollGroupJpa extends BaseEntity {

  @Column(
      name = "group_id",
      nullable = false,
      unique = true,
      updatable = false,
      columnDefinition = "UUID")
  private UUID groupId;

  @Column(name = "group_code", nullable = false, length = 50)
  private String groupCode;

  @Column(name = "type_code", nullable = false, length = 30)
  private String typeCode;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "tenant_id")
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public PayrollGroupJpa() {
    // JPA
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
  }

  public String getGroupCode() {
    return groupCode;
  }

  public void setGroupCode(String groupCode) {
    this.groupCode = groupCode;
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
