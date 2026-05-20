package com.solveria.core.experience.application.command;

import java.util.UUID;

/** Command: Solicitud de certificado digital (W14 ESS). */
public record RequestCertificateCommand(UUID personId, String certificateType, String createdBy) {}
