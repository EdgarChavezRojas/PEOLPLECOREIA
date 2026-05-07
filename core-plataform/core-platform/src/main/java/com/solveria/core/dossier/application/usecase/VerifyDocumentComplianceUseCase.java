package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.VerifyDocumentComplianceCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

public interface VerifyDocumentComplianceUseCase {

  DocumentRecord handle(VerifyDocumentComplianceCommand command);
}
