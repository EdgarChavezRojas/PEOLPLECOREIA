package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.AcknowledgeMemorandumCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

public interface AcknowledgeMemorandumUseCase {

  DocumentRecord handle(AcknowledgeMemorandumCommand command);
}

