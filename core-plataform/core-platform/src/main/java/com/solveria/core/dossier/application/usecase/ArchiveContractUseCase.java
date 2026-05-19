// Ruta: core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/usecase/ArchiveContractUseCase.java
package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.ArchiveContractCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

public interface ArchiveContractUseCase {

  DocumentRecord handle(ArchiveContractCommand command);
}

