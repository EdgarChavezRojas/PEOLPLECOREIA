package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.domain.model.LeaveTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Caso de uso: Listar todas las licencias del tenant actual de forma paginada. No requiere ningún
 * parámetro de filtro adicional.
 */
public interface ListAllLeavesUseCase {

  Page<LeaveTransaction> handle(Pageable pageable);
}
