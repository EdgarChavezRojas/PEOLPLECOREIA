package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import java.util.UUID;

public record ReportAssetInspectionCommand(
    UUID assignmentId,
    AssetDescriptor descriptor,
    boolean minorDamageReported,
    String location,
    UUID tenantId) {}

