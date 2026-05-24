package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.CheckOffboardingAssetsCommand;

public interface CheckOffboardingAssetsUseCase {

  boolean handle(CheckOffboardingAssetsCommand command);
}
