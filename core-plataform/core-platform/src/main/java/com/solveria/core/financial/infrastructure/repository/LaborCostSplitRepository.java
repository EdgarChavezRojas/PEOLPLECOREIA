package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.jpa.LaborCostSplitJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: LaborCostSplit. */
@Repository
public interface LaborCostSplitRepository extends JpaRepository<LaborCostSplitJpa, Long> {}
