package es.upm.api.domain.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetterFindCriteria {
    private Boolean opened;
    private Boolean budgetOnly;
    private String client;
    private String reference;
    private String legalProcedureTitle;

    public boolean all() {
        return opened == null && budgetOnly == null && client == null && reference == null && legalProcedureTitle == null;
    }

}
