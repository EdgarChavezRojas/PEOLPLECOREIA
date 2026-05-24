package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssignAssetCommand(
    UUID workerId,
    String assetTag,
    AssetDescriptor descriptor,
    LocalDateTime assignedAt,
    String location,
    UUID tenantId,
    String currentLocation,
    String targetLocation) {}
