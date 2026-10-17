package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.upm.miw.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalProcedureTemplate {
    private UUID id;
    @NotNull
    @NotBlank
    private String title;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal budget;
    @ListNotEmpty
    private List<LegalTask> legalTasks;
}
