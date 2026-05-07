package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.Extension;
import com.solveria.core.workforce.domain.model.vo.PartyIdentifierType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity: PartyIdentifier
 *
 * <p>Representa un documento de identidad (CI, Pasaporte). Tiene ciclo de vida (fechas de emisión y
 * caducidad) y requiere identidad propia.
 *
 * <p>Invariantes: - id_type es requerido - id_number es único - issue_date debe ser anterior a
 * expiry_date
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyIdentifier {

  private UUID identifierId;
  private UUID personId;
  private PartyIdentifierType idType;
  private String idNumber;
  private Extension extension;
  private LocalDate issueDate;
  private LocalDate expiryDate;

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

    return PartyIdentifier.builder()
        .identifierId(UUID.randomUUID())
        .personId(personId)
        .idType(idType)
        .idNumber(idNumber)
        .extension(extension)
        .issueDate(issueDate)
        .expiryDate(expiryDate)
        .build();
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
