package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.GenerateDigitalCertificateCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

public interface GenerateDigitalCertificateUseCase {

  DocumentRecord handle(GenerateDigitalCertificateCommand command);
}
