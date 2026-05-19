// Ruta: core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/command/ArchiveContractCommand.java
package com.solveria.core.dossier.application.command;

import java.util.UUID;

public record ArchiveContractCommand(
    UUID workerId, UUID contractId, String contractReference, UUID tenantId) {}

