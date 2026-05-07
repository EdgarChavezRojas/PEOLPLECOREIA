package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePersonRequest {

  @NotBlank(message = "firstName es requerido")
  private String firstName;

  @NotBlank(message = "lastName es requerido")
  private String lastName;

  @NotNull(message = "birthDate es requerido")
  private LocalDate birthDate;

  @NotBlank(message = "gender es requerido")
  private String gender; // MALE, FEMALE, OTHER

  @NotBlank(message = "globalId es requerido")
  private String globalId;

  @Email(message = "email debe ser válido")
  private String email;

  private String phone;

  private String address;

  private String maritalStatus; // SOLTERO, CASADO, VIUDO, DIVORCIADO

  private String professionTitle;
}
