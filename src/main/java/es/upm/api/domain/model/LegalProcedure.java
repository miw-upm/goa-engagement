package es.upm.api.domain.model;

import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalProcedure {
    @NotNull
    @NotBlank
    private String title;
    private LocalDate startDate;
    private LocalDate closingDate;
    private BigDecimal budget;
    private String budgetProposal;
    private Boolean vatIncluded;
    @ListNotEmpty
    private List<String> legalTasks;

    public String buildFormatBudget() {
        String result;
        if (budget == null) {
            result = budgetProposal;
        } else {
            result = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES")).format(budget);
        }
        return result + (Boolean.TRUE.equals(vatIncluded) ? " (IVA incluido)" : " (+ IVA)");
    }
}
