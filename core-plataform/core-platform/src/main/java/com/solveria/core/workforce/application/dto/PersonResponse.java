package com.solveria.core.workforce.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponse {

  private UUID personId;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private String gender;
  private String globalId;
  private Integer age;
  private String email;
  private String phone;
  private String address;
  private String maritalStatus;
  private String professionTitle;
  private Instant createdAt;
  private String DNI;
}
