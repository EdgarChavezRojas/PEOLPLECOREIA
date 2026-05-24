package com.solveria.core.workforce.application.dto;

import java.util.UUID;

public record PersonMeResponse(
        UUID personId,
        String firstName,
        String lastName,
        String email,
        String dni
) {
}
