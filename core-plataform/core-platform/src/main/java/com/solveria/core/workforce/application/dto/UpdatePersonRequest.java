package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePersonRequest {

  private String maritalStatus;
  private String professionTitle;

  @Email(message = "email debe ser valido")
  private String email;

  private String phone;
  private String address;
}
