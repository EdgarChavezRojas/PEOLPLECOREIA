// Ruta: core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/usecase/CreateDocumentRequirementsUseCase.java
package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.CreateDocumentRequirementsCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import java.util.List;

public interface CreateDocumentRequirementsUseCase {

  List<DocumentRecord> handle(CreateDocumentRequirementsCommand command);
}

