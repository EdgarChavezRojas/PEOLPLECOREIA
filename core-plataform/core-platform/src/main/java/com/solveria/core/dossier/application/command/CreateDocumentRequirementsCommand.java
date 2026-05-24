// Ruta:
// core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/command/CreateDocumentRequirementsCommand.java
package com.solveria.core.dossier.application.command;

import java.util.UUID;

public record CreateDocumentRequirementsCommand(UUID workerId, UUID tenantId) {}
