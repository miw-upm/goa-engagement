package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetterCriteria {
    private Boolean opened;
    private String owner;
    private String legalProcedureTitle;
    private String taskTitle;

    public boolean all() {
        return opened == null && owner == null && legalProcedureTitle == null;
    }
}
