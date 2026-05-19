package com.solveria.core.workforce.domain.model.vo;


public enum EmploymentCondition {
    PE("Permanente"),
    PF("Plazo fijo"),
    JU("Jubilado");

    private final String description;

    EmploymentCondition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}