package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.Extension;
import com.solveria.core.workforce.domain.model.vo.PartyIdentifierType;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity: PartyIdentifier
 *
 * <p>Representa un documento de identidad (CI, Pasaporte). Tiene ciclo de vida (fechas de emisión y
 * caducidad) y requiere identidad propia.
 *
 * <p>Invariantes: - id_type es requerido - id_number es único - issue_date debe ser anterior a
 * expiry_date
 */
public class PartyIdentifier {

  private UUID identifierId;
  private UUID personId;
  private PartyIdentifierType idType;
  private String idNumber;
  private Extension extension;
  private LocalDate issueDate;
  private LocalDate expiryDate;

  public PartyIdentifier() {}

  public PartyIdentifier(
      UUID identifierId,
      UUID personId,
      PartyIdentifierType idType,
      String idNumber,
      Extension extension,
      LocalDate issueDate,
      LocalDate expiryDate) {
    this.identifierId = identifierId;
    this.personId = personId;
    this.idType = idType;
    this.idNumber = idNumber;
    this.extension = extension;
    this.issueDate = issueDate;
    this.expiryDate = expiryDate;
  }

  public UUID getIdentifierId() {
    return identifierId;
  }

  public void setIdentifierId(UUID identifierId) {
    this.identifierId = identifierId;
  }

  public UUID getPersonId() {
    return personId;
  }

  public void setPersonId(UUID personId) {
    this.personId = personId;
  }

  public PartyIdentifierType getIdType() {
    return idType;
  }

  public void setIdType(PartyIdentifierType idType) {
    this.idType = idType;
  }

  public String getIdNumber() {
    return idNumber;
  }

  public void setIdNumber(String idNumber) {
    this.idNumber = idNumber;
  }

  public Extension getExtension() {
    return extension;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }

  public static PartyIdentifier create(
      UUID personId,
      PartyIdentifierType idType,
      String idNumber,
      Extension extension,
      LocalDate issueDate,
      LocalDate expiryDate) {
    if (idType == null || idNumber == null || idNumber.isBlank()) {
      throw new IllegalArgumentException("idType e idNumber son requeridos");
    }
    if (issueDate != null && expiryDate != null && issueDate.isAfter(expiryDate)) {
      throw new IllegalArgumentException("issueDate no puede ser posterior a expiryDate");
    }
    return new PartyIdentifier(
        UUID.randomUUID(), personId, idType, idNumber, extension, issueDate, expiryDate);
  }

  public boolean isExpired() {
    if (expiryDate == null) return false;
    return LocalDate.now().isAfter(expiryDate);
  }

  public boolean isExpiringSoon(int daysThreshold) {
    if (expiryDate == null) return false;
    LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
    return LocalDate.now().isBefore(expiryDate) && expiryDate.isBefore(thresholdDate);
  }
}
