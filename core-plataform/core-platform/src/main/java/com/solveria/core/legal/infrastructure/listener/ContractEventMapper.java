package com.solveria.core.legal.infrastructure.listener;

import com.solveria.core.legal.application.dto.ComplianceSnapshotDto;
import com.solveria.core.legal.application.dto.ProposeContractAddendumRequest;
import com.solveria.core.legal.application.dto.SalaryTermsDto;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;

@Component
public class ContractEventMapper {
    /**
     * Construye el request para una nueva adenda basándose en la última adenda vigente del contrato.
     */
    public ProposeContractAddendumRequest toProposeAddendumRequest(Contract contract) {

        // Buscamos la última adenda vigente para heredar el salario y políticas actuales
        ContractAddendum currentAddendum = contract.getAddendums().stream()
                .max(Comparator.comparing(ContractAddendum::getEffectiveFrom))
                .orElseThrow(() -> new IllegalStateException("El contrato no tiene adendas base para extraer el salario."));

        return new ProposeContractAddendumRequest(
                contract.getContractId(),
                UUID.randomUUID(),

                LocalDate.now(),
                currentAddendum.getEffectiveTo(), // Mantiene la fecha de fin de la adenda anterior
                toSalaryTermsDto(currentAddendum.getSalaryTerms()),
                toComplianceSnapshotDto(currentAddendum.getSnapshot()),
                contract.getTenantId()
        );
    }

    private SalaryTermsDto toSalaryTermsDto(SalaryTerms terms) {
        if (terms == null) return null;
        return new SalaryTermsDto(
                terms.basicSalary(), // Asumiendo que es un record o tiene el getter
                terms.totalEarnedProj(),
                terms.netSalaryProj(),
                terms.currency()
        );
    }

    private ComplianceSnapshotDto toComplianceSnapshotDto(ComplianceSnapshot snapshot) {
        if (snapshot == null) return null;
        return new ComplianceSnapshotDto(
                snapshot.smnApplied(), // Asumiendo que es un record o tiene el getter
                snapshot.taxRegime(),
                snapshot.infocalActive()
        );
    }
}