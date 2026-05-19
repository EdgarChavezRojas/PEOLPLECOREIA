package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.command.ImportTaxForm110Command;
import com.solveria.core.financial.application.port.TaxCompliancePort;
import com.solveria.core.financial.application.port.TaxForm110RepositoryPort;
import com.solveria.core.financial.domain.model.TaxForm110;
import com.solveria.core.financial.domain.service.BolivianTaxCalculationService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Importar Formulario 110 y Calcular RC-IVA. Implementa
 * TaxCompliancePort.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTaxForm110UseCase implements TaxCompliancePort {

    private final TaxForm110RepositoryPort taxForm110Repository;

    @Override
    @Transactional
    public UUID importTaxForm110(
            UUID personId,
            BigDecimal totalDeclared,
            UUID docId,
            YearMonth period,
            UUID tenantId,
            String createdBy) {
        log.info(
                "event=IMPORT_TAX_FORM_110 personId={} period={} totalDeclared={}",
                personId,
                period,
                totalDeclared);

        ImportTaxForm110Command cmd = new ImportTaxForm110Command(personId, totalDeclared, docId, period, tenantId,
                createdBy);

        TaxForm110 form = TaxForm110.importForm(
                cmd.personId(),
                cmd.totalDeclared(),
                cmd.docId(),
                cmd.period(),
                cmd.tenantId(),
                cmd.createdBy());

        taxForm110Repository.save(form);

        log.info(
                "event=TAX_FORM_110_IMPORTED formId={} verifiedCredit={}",
                form.getFormId(),
                form.getVerifiedCredit());
        return form.getFormId();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRcIva(UUID personId, BigDecimal sueldoNeto, YearMonth period) {
        log.info(
                "event=CALCULATE_RC_IVA personId={} sueldoNeto={} period={}", personId, sueldoNeto, period);

        // Buscar Form 110 verificados para el período
        List<TaxForm110> forms = taxForm110Repository.findByPersonIdAndPeriod(
                personId, period, null); // tenantId resolved by filter

        BigDecimal totalVerifiedCredit = forms.stream().map(TaxForm110::getVerifiedCredit).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        BigDecimal rcIva = BolivianTaxCalculationService.calculateRcIva(sueldoNeto, totalVerifiedCredit);

        log.info(
                "event=RC_IVA_CALCULATED personId={} rcIva={} verifiedCredit={}",
                personId,
                rcIva,
                totalVerifiedCredit);
        return rcIva;
    }
}
