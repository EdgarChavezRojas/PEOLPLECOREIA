package com.solveria.core.dossier.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class DocumentMetadataEmbeddable {

  @Column(name = "storage_id", nullable = false)
  private UUID storageId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "hash_sha256", nullable = false)
  private String hashSha256;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;
}
