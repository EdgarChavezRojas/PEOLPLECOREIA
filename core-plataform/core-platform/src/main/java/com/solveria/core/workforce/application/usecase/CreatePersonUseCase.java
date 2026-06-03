package com.solveria.core.workforce.application.usecase;

import com.solveria.core.iam.application.port.UserRepositoryPort;
import com.solveria.core.iam.domain.model.User;
import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.workforce.application.dto.CreatePersonRequest;
import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.exception.PersonAlreadyExistsException;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.Gender;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import com.solveria.core.workforce.infrastructure.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePersonUseCase {

  private final PersonRepositoryPort personRepositoryPort;
  private final PersonMapper personMapper;
  private final UserRepositoryPort userRepositoryPort;
  private final PasswordEncoder passwordEncoder;

  // SERIALIZABLE isolation: prevents two simultaneous requests from both passing the
  // existsByGlobalId / findByCi checks before either has committed. The DB unique
  // constraints on global_id and DNI remain the ultimate safety net.
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public PersonResponse execute(CreatePersonRequest request) {
    if (personRepositoryPort.existsByGlobalId(request.getGlobalId())) {
      throw new PersonAlreadyExistsException("PersonID ya existe: " + request.getGlobalId());
    }
    if (personRepositoryPort.findByCi(request.getDNI()).isPresent()) {
      throw new PersonAlreadyExistsException(
          "La persona con DNI/CI " + request.getDNI() + " ya existe.");
    }

    // Generate formatted email and username based on first and last names (handling multiple
    // words/lastnames)
    String cleanFirst = sanitize(request.getFirstName());
    String cleanLast = sanitize(request.getLastName());

    String emailPartFirst = cleanFirst.replaceAll("\\s+", ".");
    String emailPartLast = cleanLast.replaceAll("\\s+", ".");
    String baseEmail = emailPartFirst + "." + emailPartLast;

    String userPartFirst = cleanFirst.replaceAll("\\s+", "_");
    String userPartLast = cleanLast.replaceAll("\\s+", "_");
    String baseUsername = userPartFirst + "_" + userPartLast;

    String username = baseUsername;
    int counter = 1;
    while (userRepositoryPort.findByUsername(username).isPresent()) {
      username = baseUsername + counter;
      counter++;
    }

    String email = baseEmail + "@solveria.com";
    counter = 1;
    while (userRepositoryPort.findByEmail(email).isPresent()) {
      email = baseEmail + counter + "@solveria.com";
      counter++;
    }

    // Generate secure temporary password
    String rawPassword = generateSecurePassword();
    String hashedPassword = passwordEncoder.encode(rawPassword);

    // TODO: En el futuro, cuando existan más roles en RRHH, podemos pasar una lista dinámica de
    // roles desde
    // el request (ej. request.getRoleIds()) en lugar de fijar por defecto el rol de Empleado (ID
    // 2).
    // createdAt y los campos de auditoría se dejan a null: JPA Auditing (@CreatedDate,
    // @LastModifiedDate)
    // los poblará automáticamente al persistir. createdBy toma el usuario real del contexto JWT.
    String createdBy = SecurityUserContext.getUserIdentifier();
    User newUser =
        new User(
            null,
            username,
            email,
            hashedPassword,
            true,
            java.util.Set.of(2L), // Rol por defecto: 2 (EMPLOYEE)
            request.getTenantId(),
            null,
            null, // createdAt → @CreatedDate lo puebla JPA Auditing
            createdBy, // createdBy → usuario real del JWT (o "system" como fallback)
            null, // lastModifiedAt → @LastModifiedDate lo puebla JPA Auditing
            null // lastModifiedBy → @LastModifiedBy lo puebla JPA Auditing
            );
    User savedUser = userRepositoryPort.save(newUser);

    // Create and save Person
    ContactPoint contact = ContactPoint.create(email, request.getPhone(), request.getAddress());

    Gender gender = Gender.valueOf(request.getGender().toUpperCase());
    MaritalStatus maritalStatus =
        request.getMaritalStatus() != null
            ? MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase())
            : null;

    Person person =
        Person.create(
            request.getFirstName(),
            request.getLastName(),
            request.getBirthDate(),
            gender,
            maritalStatus,
            request.getProfessionTitle(),
            request.getGlobalId(),
            contact,
            request.getDNI(),
            request.getTenantId());
    person.setUserId(savedUser.getId());

    Person savedPerson = personRepositoryPort.save(person);

    log.info(
        "event=PERSON_CREATE_SUCCESS personId={} firstName={} userId={}",
        savedPerson.getPersonId(),
        savedPerson.getFirstName(),
        savedUser.getId());

    PersonResponse response = personMapper.toResponse(savedPerson);
    response.setUsername(username);
    response.setTempPassword(rawPassword);
    return response;
  }

  private String sanitize(String input) {
    if (input == null) {
      return "";
    }
    String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
    normalized = normalized.replaceAll("\\p{M}", "");
    return normalized.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
  }

  private String generateSecurePassword() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    java.security.SecureRandom random = new java.security.SecureRandom();
    StringBuilder sb = new StringBuilder(12);
    for (int i = 0; i < 12; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}
