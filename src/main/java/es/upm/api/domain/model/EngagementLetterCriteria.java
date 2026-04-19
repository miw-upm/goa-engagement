package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetterCriteria {
    private Boolean opened;
    private Boolean budgetOnly;
    private String client;
    private String legalProcedureTitle;
    private String taskTitle;

    public boolean all() {
        return opened == null && client == null && legalProcedureTitle == null;
    }

}
