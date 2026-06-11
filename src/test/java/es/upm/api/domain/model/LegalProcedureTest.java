package es.upm.api.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegalProcedureTest {

    @Test
    void testBuildFormatBudgetWithBudgetProposalAndVatIncluded() {
        LegalProcedure procedure = LegalProcedure.builder()
                .budgetProposal("Pendiente de valorar")
                .vatIncluded(true)
                .build();

        assertThat(procedure.buildFormatBudget())
                .isEqualTo("Pendiente de valorar (IVA incluido)");
    }

    @Test
    void testBuildFormatBudgetWithBudgetProposalAndVatNotIncluded() {
        LegalProcedure procedure = LegalProcedure.builder()
                .budgetProposal("Pendiente de valorar")
                .vatIncluded(false)
                .build();

        assertThat(procedure.buildFormatBudget())
                .isEqualTo("Pendiente de valorar (+ IVA)");
    }
}
