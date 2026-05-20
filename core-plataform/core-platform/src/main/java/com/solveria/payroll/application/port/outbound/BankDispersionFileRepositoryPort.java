package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import java.util.Optional;
import java.util.UUID;

public interface BankDispersionFileRepositoryPort {
  BankDispersionFile save(BankDispersionFile file);

  Optional<BankDispersionFile> findById(UUID id);
}
