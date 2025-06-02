package es.upm.api.domain.model;

import es.upm.api.domain.model.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}
