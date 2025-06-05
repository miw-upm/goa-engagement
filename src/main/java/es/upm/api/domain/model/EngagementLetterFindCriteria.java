package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementLetterFindCriteria {
    private Boolean opened;
    private String owner;
    private String legalProcedureTitle;

    public boolean all() {
        return opened == null && owner == null && legalProcedureTitle == null;
    }
}
