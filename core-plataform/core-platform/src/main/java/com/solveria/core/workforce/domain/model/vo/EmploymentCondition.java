package com.solveria.core.workforce.domain.model.vo;


public enum EmploymentCondition {
  PE("Permanente"),
  PF("Plazo fijo"),
  JU("Jubilado");
   final String description;
    EmploymentCondition(String label) {
        this.description = label;
    }
}

