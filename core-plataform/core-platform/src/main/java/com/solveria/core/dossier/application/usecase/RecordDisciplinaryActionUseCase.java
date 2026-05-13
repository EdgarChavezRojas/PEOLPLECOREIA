package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.RecordDisciplinaryActionCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

public interface RecordDisciplinaryActionUseCase {

  DocumentRecord handle(RecordDisciplinaryActionCommand command);
}

