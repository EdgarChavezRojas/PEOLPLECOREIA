package com.solveria.core.dossier.application.port;

import com.solveria.core.dossier.domain.model.DocumentRecord;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRecordRepositoryPort {

  DocumentRecord save(DocumentRecord record);

  Optional<DocumentRecord> findById(UUID docId);
}
