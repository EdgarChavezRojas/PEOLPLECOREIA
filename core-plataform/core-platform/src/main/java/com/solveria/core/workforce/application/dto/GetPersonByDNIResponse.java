package com.solveria.core.workforce.application.dto;

public record GetPersonByDNIResponse(
        String globalId,
        String firstName,
        String lastName,
        String DNI,
        String email,
        String phoneNumber
) {

}
