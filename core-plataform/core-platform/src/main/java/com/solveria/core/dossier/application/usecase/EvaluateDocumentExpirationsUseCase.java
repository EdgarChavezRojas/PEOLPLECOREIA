package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.EvaluateDocumentExpirationsCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import java.util.List;

public interface EvaluateDocumentExpirationsUseCase {

  List<DocumentRecord> handle(EvaluateDocumentExpirationsCommand command);
}
