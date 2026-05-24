package com.solveria.scheduling.application.port.inbound;

import com.solveria.scheduling.domain.model.enums.PunchType;
import com.solveria.scheduling.domain.model.vo.GeoValidation;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TimeTrackingUseCase {
  void registerPunch(
      UUID relationshipId,
      LocalDateTime punchTime,
      PunchType punchType,
      String deviceId,
      GeoValidation geoValidation);
}
