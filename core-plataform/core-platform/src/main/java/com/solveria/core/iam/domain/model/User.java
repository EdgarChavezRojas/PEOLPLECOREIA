package com.solveria.core.iam.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Pure domain model for User - no persistence framework dependencies.
 *
 * <p>Represents a user in the system with their roles and permissions.
 */
public class User {

  private final Long id;
  private String username;
  private String email;
  private String password;
  private boolean active;
  private Set<Long> roleIds = new HashSet<>();
  private UUID tenantId;
  private Long version;
  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime lastModifiedAt;
  private String lastModifiedBy;

  protected User() {
    this.id = null;
  }

  public User(String username, String email, String password, boolean active) {
    this.id = null;
    this.username = username;
    this.email = email;
    this.password = password;
    this.active = active;
  }

  public User(
      Long id,
      String username,
      String email,
      String password,
      boolean active,
      Set<Long> roleIds,
      UUID tenantId,
      Long version,
      LocalDateTime createdAt,
      String createdBy,
      LocalDateTime lastModifiedAt,
      String lastModifiedBy) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.active = active;
    this.roleIds = roleIds != null ? new HashSet<>(roleIds) : new HashSet<>();
    this.tenantId = tenantId;
    this.version = version;
    this.createdAt = createdAt;
    this.createdBy = createdBy;
    this.lastModifiedAt = lastModifiedAt;
    this.lastModifiedBy = lastModifiedBy;
  }

  public void assignRoleIds(Set<Long> roleIds) {
    this.roleIds.clear();
    if (roleIds != null) {
      this.roleIds.addAll(roleIds);
    }
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<Long> getRoleIds() {
    return new HashSet<>(roleIds);
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public Long getVersion() {
    return version;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public LocalDateTime getLastModifiedAt() {
    return lastModifiedAt;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }
}
