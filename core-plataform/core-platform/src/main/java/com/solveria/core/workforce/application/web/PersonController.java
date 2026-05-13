package com.solveria.core.workforce.application.web;




import com.solveria.core.workforce.application.dto.CreatePersonRequest;
import com.solveria.core.workforce.application.dto.PersonResponse;
import com.solveria.core.workforce.application.usecase.CreatePersonUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
@Tag(name = "Person", description = "Gestión de Identidad Civil Única (Master Data)")
public class PersonController {

    private final CreatePersonUseCase createPersonUseCase;

    @PostMapping
    @Operation(summary = "Crear registro maestro de persona",
            description = "Registra una nueva identidad física en el sistema Core.")
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody CreatePersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createPersonUseCase.execute(request));
    }
}