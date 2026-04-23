package es.upm.api.domain.model;

import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    private Boolean vatIncluded;
    @ListNotEmpty
    private List<String> legalTasks;

    public String buildFormatBudget() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("es", "ES"));
        df.setDecimalFormatSymbols(symbols);
        return df.format(budget) + " €" + (Boolean.TRUE.equals(vatIncluded) ? " (IVA incluido)" : " (+ IVA)");
    }
}
