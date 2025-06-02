package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.LegalProcedureTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalProcedureEntity {
    private String title;
    private LocalDate startDate;
    private LocalDate closingDate;
    private BigDecimal budget;
    private Boolean vatIncluded;
    private List<String> legalTasks;

    public LegalProcedureEntity(LegalProcedureTemplate legalProcedureTemplate) {
        BeanUtils.copyProperties(legalProcedureTemplate, this);
    }

    public LegalProcedure toLegalProcedure() {
        LegalProcedure legalProcedure = new LegalProcedure();
        BeanUtils.copyProperties(this, legalProcedure);
        return legalProcedure;
    }
}

