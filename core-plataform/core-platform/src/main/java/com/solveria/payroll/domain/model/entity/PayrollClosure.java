package com.solveria.payroll.domain.model.entity;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.domain.model.vo.ClosureStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

public class PayrollClosure {

  private UUID id;
  private UUID runRef;
  private ClosureStatus status;
  private String integrityHash;
  private UUID tenantId;

  public PayrollClosure(
      UUID id, UUID runRef, ClosureStatus status, String integrityHash, UUID tenantId) {
    this.id = id;
    this.runRef = runRef;
    this.status = status;
    this.integrityHash = integrityHash;
    this.tenantId = tenantId;
  }

  public static PayrollClosure initialize(UUID id, UUID runRef, UUID tenantId) {
    return new PayrollClosure(id, runRef, ClosureStatus.PROCESANDO, null, tenantId);
  }

  public void seal() {
    try {
      String dataToHash = this.runRef.toString() + Instant.now().toEpochMilli();
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
      this.integrityHash = bytesToHex(encodedhash);
      this.status = ClosureStatus.CERRADO_EXITO;
    } catch (NoSuchAlgorithmException e) {
      this.status = ClosureStatus.ERROR;
      throw new SolverExceptionImpl("PAYROLL_HASH_GENERATION_FAILED");
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public void markAsError() {
    this.status = ClosureStatus.ERROR;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRunRef() {
    return runRef;
  }

  public ClosureStatus getStatus() {
    return status;
  }

  public String getIntegrityHash() {
    return integrityHash;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
