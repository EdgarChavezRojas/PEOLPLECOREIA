package com.solveria.core.legal.application.port;

import java.util.UUID;

public interface ProjectResolutionPort {
  UUID getDefaultProjectIdForTenant(UUID tenantId);
}
