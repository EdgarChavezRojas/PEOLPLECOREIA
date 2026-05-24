package com.solveria.core.workforce.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgUnitResponse {

  private UUID unitId;
  private UUID parentId;
  private String name;
  private String unitType;
  private String costCode;
  private String costDescription;
  private Boolean isRoot;
  private String geoCoords;
}
